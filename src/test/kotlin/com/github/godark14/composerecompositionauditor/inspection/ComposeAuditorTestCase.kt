package com.github.godark14.composerecompositionauditor.inspection

import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Adds minimal stub declarations for the Compose/kotlinx.immutable symbols
 * our inspections resolve against, since the test project has no real
 * Compose/Android dependency. @Composable/@Preview detection is text-based
 * (no resolution needed), but @Stable/@Immutable and ImmutableList DO need
 * to resolve to a real declaration for TypeDescriptorExtractor to work.
 */
abstract class ComposeAuditorTestCase : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()
        myFixture.addFileToProject(
            "androidx/compose/runtime/Annotations.kt",
            """
            package androidx.compose.runtime

            @Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
            annotation class Composable

            @Target(AnnotationTarget.CLASS)
            annotation class Stable

            @Target(AnnotationTarget.CLASS)
            annotation class Immutable
            """.trimIndent(),
        )
        myFixture.addFileToProject(
            "kotlinx/collections/immutable/Interfaces.kt",
            """
            package kotlinx.collections.immutable

            interface ImmutableList<out E> : List<E>
            interface ImmutableMap<K, out V> : Map<K, V>
            interface ImmutableSet<out E> : Set<E>
            """.trimIndent(),
        )
    }
}