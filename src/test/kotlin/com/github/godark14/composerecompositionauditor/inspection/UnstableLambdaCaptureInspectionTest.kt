package com.github.godark14.composerecompositionauditor.inspection

import com.intellij.lang.annotation.HighlightSeverity

class UnstableLambdaCaptureInspectionTest : ComposeAuditorTestCase() {

    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(UnstableLambdaCaptureInspection())
    }

    private fun warnings(): List<String> =
        myFixture.doHighlighting(HighlightSeverity.WARNING).map { it.description }

    fun `test flags lambda capturing unprotected local var`() {
        myFixture.configureByText(
            "Test.kt",
            """
            import androidx.compose.runtime.Composable

            @Composable
            fun MyScreen() {
                var counter = 0
                val onClick = { counter++ }
            }
            """.trimIndent(),
        )
        assertTrue(warnings().any { it.contains("counter") })
    }

    fun `test does not flag lambda capturing remember-delegated var`() {
        myFixture.configureByText(
            "Test.kt",
            """
            import androidx.compose.runtime.Composable

            @Composable
            fun MyScreen() {
                var counter by remember { mutableStateOf(0) }
                val onClick = { counter++ }
            }
            """.trimIndent(),
        )
        assertTrue(warnings().none { it.contains("counter") })
    }

    fun `test does not flag capture inside the remember initializer lambda itself`() {
        myFixture.configureByText(
            "Test.kt",
            """
            import androidx.compose.runtime.Composable

            @Composable
            fun MyScreen() {
                var seed = 42
                val state = remember { seed }
            }
            """.trimIndent(),
        )
        assertTrue(warnings().none { it.contains("seed") })
    }

    fun `test does not flag val capture`() {
        myFixture.configureByText(
            "Test.kt",
            """
            import androidx.compose.runtime.Composable

            @Composable
            fun MyScreen() {
                val label = "Click me"
                val onClick = { println(label) }
            }
            """.trimIndent(),
        )
        assertTrue(warnings().none { it.contains("label") })
    }

    fun `test does not flag Preview composable`() {
        myFixture.configureByText(
            "Test.kt",
            """
            import androidx.compose.runtime.Composable

            annotation class Preview

            @Preview
            @Composable
            fun MyScreenPreview() {
                var counter = 0
                val onClick = { counter++ }
            }
            """.trimIndent(),
        )
        assertTrue(warnings().none { it.contains("counter") })
    }
}