package com.github.godark14.composerecompositionauditor.stability

/**
 * Simplified, PSI-independent description of a Kotlin type, built to mirror
 * how the Compose compiler classifies stability. Kept separate from PSI so
 * the inference rules can be unit-tested without an IDE instance.
 */
sealed interface TypeDescriptor {

    /** Int, Long, Boolean, String, etc. Always stable. */
    data class PrimitiveType(val name: String) : TypeDescriptor

    /** A function type, e.g. () -> Unit. Always stable by itself; the
     * capture analysis for lambdas is handled separately. */
    data class FunctionType(val signature: String) : TypeDescriptor

    /** kotlin.collections.List / Map / Set and their Mutable* counterparts.
     * These are interfaces backed by potentially-mutable implementations,
     * so the Compose compiler treats them as unstable unless wrapped. */
    data class MutableCollectionType(val name: String) : TypeDescriptor

    /** kotlinx.collections.immutable.ImmutableList / ImmutableMap / etc,
     * or any type explicitly annotated @Immutable. */
    data class ImmutableCollectionType(val name: String) : TypeDescriptor

    /**
     * A user-defined or library class/interface.
     *
     * @param isAnnotatedStable true if annotated with @Stable
     * @param isAnnotatedImmutable true if annotated with @Immutable
     * @param hasVarProperty true if any constructor property or field is `var`
     * @param isExternal true if the class body isn't visible to the compiler
     *   (defined in another module/library with no source attached) — the
     *   Compose compiler cannot prove stability for these and treats them
     *   as unstable by default.
     */
    data class ClassType(
        val name: String,
        val isAnnotatedStable: Boolean = false,
        val isAnnotatedImmutable: Boolean = false,
        val hasVarProperty: Boolean = false,
        val isExternal: Boolean = false,
    ) : TypeDescriptor
}