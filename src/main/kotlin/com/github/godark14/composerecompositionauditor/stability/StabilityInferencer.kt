package com.github.godark14.composerecompositionauditor.stability

import com.github.godark14.composerecompositionauditor.stability.TypeDescriptor.ClassType
import com.github.godark14.composerecompositionauditor.stability.TypeDescriptor.FunctionType
import com.github.godark14.composerecompositionauditor.stability.TypeDescriptor.ImmutableCollectionType
import com.github.godark14.composerecompositionauditor.stability.TypeDescriptor.MutableCollectionType
import com.github.godark14.composerecompositionauditor.stability.TypeDescriptor.PrimitiveType

/**
 * Reproduces (a first-pass, conservative subset of) the Compose compiler's
 * stability inference, so the IDE can flag likely-unstable @Composable
 * parameters before compilation.
 */
object StabilityInferencer {

    fun infer(type: TypeDescriptor): Stability = when (type) {
        is PrimitiveType -> Stability.STABLE
        is FunctionType -> Stability.STABLE
        is ImmutableCollectionType -> Stability.STABLE
        is MutableCollectionType -> Stability.UNSTABLE
        is ClassType -> inferClassStability(type)
    }

    private fun inferClassStability(type: ClassType): Stability {
        if (type.isAnnotatedStable || type.isAnnotatedImmutable) {
            return Stability.STABLE
        }
        if (type.hasVarProperty) {
            return Stability.UNSTABLE
        }
        if (type.isExternal) {
            // Compiler can't see the body — conservative default.
            return Stability.UNKNOWN
        }
        return Stability.STABLE
    }
}