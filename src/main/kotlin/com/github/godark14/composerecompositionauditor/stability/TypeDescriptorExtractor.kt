package com.github.godark14.composerecompositionauditor.stability

import com.github.godark14.composerecompositionauditor.stability.TypeDescriptor.ClassType
import com.github.godark14.composerecompositionauditor.stability.TypeDescriptor.FunctionType
import com.github.godark14.composerecompositionauditor.stability.TypeDescriptor.ImmutableCollectionType
import com.github.godark14.composerecompositionauditor.stability.TypeDescriptor.KnownStableType
import com.github.godark14.composerecompositionauditor.stability.TypeDescriptor.MutableCollectionType
import com.github.godark14.composerecompositionauditor.stability.TypeDescriptor.PrimitiveType
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.components.*
import org.jetbrains.kotlin.analysis.api.symbols.KaClassSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaPropertySymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaSymbolOrigin
import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.analysis.api.types.KaFunctionType
import org.jetbrains.kotlin.analysis.api.types.KaType
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.psi.KtParameter

object TypeDescriptorExtractor {

    private val PRIMITIVE_FQ_NAMES = setOf(
        "kotlin.Int", "kotlin.Long", "kotlin.Short", "kotlin.Byte",
        "kotlin.Float", "kotlin.Double", "kotlin.Boolean", "kotlin.Char",
        "kotlin.String", "kotlin.Unit",
    )

    private val MUTABLE_COLLECTION_FQ_NAMES = setOf(
        "kotlin.collections.List", "kotlin.collections.MutableList",
        "kotlin.collections.Map", "kotlin.collections.MutableMap",
        "kotlin.collections.Set", "kotlin.collections.MutableSet",
    )

    private val IMMUTABLE_COLLECTION_FQ_NAMES = setOf(
        "kotlinx.collections.immutable.ImmutableList",
        "kotlinx.collections.immutable.ImmutableMap",
        "kotlinx.collections.immutable.ImmutableSet",
        "kotlinx.collections.immutable.PersistentList",
        "kotlinx.collections.immutable.PersistentMap",
        "kotlinx.collections.immutable.PersistentSet",
    )

    /**
     * Types known to be stable by library contract, even when defined
     * outside this module with no visible source and no @Stable/@Immutable
     * annotation resolvable from binary metadata. Kept small and
     * deliberately conservative — this is an escape hatch for well-known,
     * widely-used types, not a general substitute for real annotations.
     */
    private val KNOWN_STABLE_EXTERNAL_FQ_NAMES = setOf(
        "androidx.compose.ui.Modifier",
        "androidx.compose.runtime.State",
        "androidx.compose.runtime.MutableState",
        "androidx.compose.runtime.snapshots.SnapshotStateList",
        "androidx.compose.runtime.snapshots.SnapshotStateMap",
        "kotlinx.coroutines.flow.StateFlow",
        "kotlinx.coroutines.flow.SharedFlow",
    )

    private const val STABLE_ANNOTATION_FQN = "androidx.compose.runtime.Stable"
    private const val IMMUTABLE_ANNOTATION_FQN = "androidx.compose.runtime.Immutable"

    fun extract(parameter: KtParameter): TypeDescriptor? = analyze(parameter) {
        extractFromType(this, parameter.returnType)
    }

    private fun extractFromType(session: KaSession, type: KaType): TypeDescriptor? = with(session) {
        if (type is KaFunctionType) {
            return@with FunctionType(type.toString())
        }

        val classType = type as? KaClassType ?: return@with null
        val fqName = classType.classId.asFqNameString()

        when {
            fqName in PRIMITIVE_FQ_NAMES ->
                PrimitiveType(fqName.substringAfterLast('.'))

            fqName in MUTABLE_COLLECTION_FQ_NAMES ->
                MutableCollectionType(fqName.substringAfterLast('.'))

            fqName in IMMUTABLE_COLLECTION_FQ_NAMES ->
                ImmutableCollectionType(fqName.substringAfterLast('.'))

            fqName in KNOWN_STABLE_EXTERNAL_FQ_NAMES ->
                KnownStableType(fqName.substringAfterLast('.'))

            else -> {
                val classSymbol = classType.symbol as? KaClassSymbol ?: return@with null
                ClassType(
                    name = fqName,
                    isAnnotatedStable = classSymbol.hasAnnotation(STABLE_ANNOTATION_FQN),
                    isAnnotatedImmutable = classSymbol.hasAnnotation(IMMUTABLE_ANNOTATION_FQN),
                    hasVarProperty = hasVarProperty(session, classSymbol),
                    isExternal = classSymbol.origin != KaSymbolOrigin.SOURCE,
                )
            }
        }
    }

    private fun KaClassSymbol.hasAnnotation(fqName: String): Boolean =
        annotations.any { it.classId?.asFqNameString() == fqName }

    private fun hasVarProperty(session: KaSession, classSymbol: KaClassSymbol): Boolean =
        with(session) {
            classSymbol.declaredMemberScope.callables
                .filterIsInstance<KaPropertySymbol>()
                .any { !it.isVal }
        }

}