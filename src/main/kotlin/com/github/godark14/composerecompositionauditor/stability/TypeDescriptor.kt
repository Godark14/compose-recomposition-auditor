package com.github.godark14.composerecompositionauditor.stability

sealed interface TypeDescriptor {

    data class PrimitiveType(val name: String) : TypeDescriptor

    data class FunctionType(val signature: String) : TypeDescriptor

    data class MutableCollectionType(val name: String) : TypeDescriptor

    data class ImmutableCollectionType(val name: String) : TypeDescriptor

    /**
     * A type known to be stable by convention or well-established library
     * contract, even though it's external and unannotated — mirrors the
     * Compose compiler's own "stability configuration" escape hatch for
     * types it can't otherwise prove stable (e.g. androidx.compose.ui.Modifier,
     * kotlinx.coroutines.flow.StateFlow).
     */
    data class KnownStableType(val name: String) : TypeDescriptor

    data class ClassType(
        val name: String,
        val isAnnotatedStable: Boolean = false,
        val isAnnotatedImmutable: Boolean = false,
        val hasVarProperty: Boolean = false,
        val isExternal: Boolean = false,
    ) : TypeDescriptor
}