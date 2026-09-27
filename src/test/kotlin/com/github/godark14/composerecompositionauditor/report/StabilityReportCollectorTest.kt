package com.github.godark14.composerecompositionauditor.report

import com.github.godark14.composerecompositionauditor.inspection.ComposeAuditorTestCase
import com.github.godark14.composerecompositionauditor.stability.IssueCategory

class StabilityReportCollectorTest : ComposeAuditorTestCase() {

    private fun collect(fileName: String, code: String): List<StabilityReportEntry> {
        val file = myFixture.configureByText(fileName, code.trimIndent())
        return StabilityReportCollector(project).collect(listOf(file.virtualFile))
    }

    fun `test collects unstable List parameter`() {
        val entries = collect(
            "Test.kt",
            """
            import androidx.compose.runtime.Composable

            @Composable
            fun MyScreen(items: List<String>) {}
            """
        )

        assertTrue(entries.any {
            it.category == IssueCategory.UNSTABLE_PARAMETER &&
                    it.description.contains("items") &&
                    it.functionName == "MyScreen"
        })
    }

    fun `test does not collect ImmutableList parameter`() {
        val entries = collect(
            "Test.kt",
            """
            import androidx.compose.runtime.Composable
            import kotlinx.collections.immutable.ImmutableList

            @Composable
            fun MyScreen(items: ImmutableList<String>) {}
            """
        )

        assertTrue(entries.none { it.description.contains("items") })
    }

    fun `test collects data class with var property`() {
        val entries = collect(
            "Test.kt",
            """
            import androidx.compose.runtime.Composable

            data class UiState(var count: Int)

            @Composable
            fun MyScreen(state: UiState) {}
            """
        )

        assertTrue(entries.any {
            it.category == IssueCategory.UNSTABLE_PARAMETER && it.description.contains("state")
        })
    }

    fun `test does not collect class annotated Stable`() {
        val entries = collect(
            "Test.kt",
            """
            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.Stable

            @Stable
            data class UiState(var count: Int)

            @Composable
            fun MyScreen(state: UiState) {}
            """
        )

        assertTrue(entries.none { it.description.contains("state") })
    }

    fun `test does not collect Modifier parameter`() {
        val entries = collect(
            "Test.kt",
            """
            import androidx.compose.runtime.Composable
            import androidx.compose.ui.Modifier

            @Composable
            fun MyScreen(modifier: Modifier) {}
            """
        )

        assertTrue(entries.none { it.description.contains("modifier") })
    }

    fun `test collects unstable lambda capture`() {
        val entries = collect(
            "Test.kt",
            """
            import androidx.compose.runtime.Composable

            @Composable
            fun MyScreen() {
                var counter = 0
                val onClick = { counter++ }
            }
            """
        )

        assertTrue(entries.any {
            it.category == IssueCategory.UNSTABLE_LAMBDA_CAPTURE &&
                    it.description.contains("counter")
        })
    }

    fun `test does not collect remember-wrapped state capture`() {
        val entries = collect(
            "Test.kt",
            """
            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.remember
            import androidx.compose.runtime.mutableStateOf
            import androidx.compose.runtime.getValue
            import androidx.compose.runtime.setValue

            @Composable
            fun MyScreen() {
                var counter by remember { mutableStateOf(0) }
                val onClick = { counter++ }
            }
            """
        )

        assertTrue(entries.none { it.category == IssueCategory.UNSTABLE_LAMBDA_CAPTURE })
    }

    fun `test skips Preview annotated composables`() {
        val entries = collect(
            "Test.kt",
            """
            import androidx.compose.runtime.Composable

            annotation class Preview

            @Preview
            @Composable
            fun MyScreenPreview(items: List<String>) {}
            """
        )

        assertTrue(entries.isEmpty())
    }

    fun `test skips non-composable function`() {
        val entries = collect("Test.kt", "fun regularFunction(items: List<String>) {}")
        assertTrue(entries.isEmpty())
    }

    fun `test aggregates issues across multiple files`() {
        val file1 = myFixture.configureByText(
            "MultiA.kt",
            """
            import androidx.compose.runtime.Composable

            @Composable
            fun ComposableA(items: List<String>) {}
            """.trimIndent()
        )
        val file2 = myFixture.addFileToProject(
            "MultiB.kt",
            """
            import androidx.compose.runtime.Composable

            @Composable
            fun ComposableB(items: MutableMap<String, String>) {}
            """.trimIndent()
        )

        val entries = StabilityReportCollector(project).collect(listOf(file1.virtualFile, file2.virtualFile))

        assertTrue(entries.any { it.functionName == "ComposableA" })
        assertTrue(entries.any { it.functionName == "ComposableB" })
    }
}