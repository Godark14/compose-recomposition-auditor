package com.github.godark14.composerecompositionauditor.inspection

import com.intellij.lang.annotation.HighlightSeverity

class UnstableComposableParameterInspectionTest : ComposeAuditorTestCase() {

    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(UnstableComposableParameterInspection())
    }

    private fun warnings(): List<String> =
        myFixture.doHighlighting(HighlightSeverity.WARNING).map { it.description }

    fun `test flags mutable List parameter`() {
        myFixture.configureByText(
            "Test.kt",
            """
            import androidx.compose.runtime.Composable

            @Composable
            fun MyScreen(items: List<String>) {}
            """.trimIndent(),
        )
        assertTrue(warnings().any { it.contains("items") && it.contains("unstable") })
    }

    fun `test does not flag ImmutableList parameter`() {
        myFixture.configureByText(
            "Test.kt",
            """
            import androidx.compose.runtime.Composable
            import kotlinx.collections.immutable.ImmutableList

            @Composable
            fun MyScreen(items: ImmutableList<String>) {}
            """.trimIndent(),
        )
        assertTrue(warnings().none { it.contains("items") })
    }

    fun `test flags data class with var property`() {
        myFixture.configureByText(
            "Test.kt",
            """
            import androidx.compose.runtime.Composable

            data class UiState(var count: Int)

            @Composable
            fun MyScreen(state: UiState) {}
            """.trimIndent(),
        )
        assertTrue(warnings().any { it.contains("state") && it.contains("unstable") })
    }

    fun `test does not flag class annotated Stable`() {
        myFixture.configureByText(
            "Test.kt",
            """
            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.Stable

            @Stable
            data class UiState(var count: Int)

            @Composable
            fun MyScreen(state: UiState) {}
            """.trimIndent(),
        )
        assertTrue(warnings().none { it.contains("state") })
    }

    fun `test does not flag non-composable function`() {
        myFixture.configureByText("Test.kt", "fun regularFunction(items: List<String>) {}")
        assertTrue(warnings().none { it.contains("items") })
    }

    fun `test does not flag Preview composable`() {
        myFixture.configureByText(
            "Test.kt",
            """
            import androidx.compose.runtime.Composable

            annotation class Preview

            @Preview
            @Composable
            fun MyScreenPreview(items: List<String>) {}
            """.trimIndent(),
        )
        assertTrue(warnings().none { it.contains("items") })
    }

    fun `test does not flag Modifier parameter even though unannotated`() {
        myFixture.configureByText(
            "Test.kt",
            """
        import androidx.compose.runtime.Composable
        import androidx.compose.ui.Modifier

        @Composable
        fun MyScreen(modifier: Modifier) {}
        """.trimIndent(),
        )
        assertTrue(warnings().none { it.contains("modifier") })
    }
}