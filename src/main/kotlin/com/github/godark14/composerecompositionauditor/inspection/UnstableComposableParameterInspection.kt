package com.github.godark14.composerecompositionauditor.inspection

import com.github.godark14.composerecompositionauditor.stability.Stability
import com.github.godark14.composerecompositionauditor.stability.StabilityInferencer
import com.github.godark14.composerecompositionauditor.stability.TypeDescriptorExtractor
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtVisitorVoid

private const val COMPOSABLE_ANNOTATION_FQN = "androidx.compose.runtime.Composable"

class UnstableComposableParameterInspection : LocalInspectionTool() {

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): KtVisitorVoid =
        object : KtVisitorVoid() {
            override fun visitNamedFunction(function: KtNamedFunction) {
                if (!function.isComposable()) return
                function.valueParameters.forEach { parameter -> checkParameter(parameter, holder) }
            }
        }

    private fun KtNamedFunction.isComposable(): Boolean =
        annotationEntries.any { it.shortName?.asString() == "Composable" }
    // Note: this checks the short name only. A production version should
    // resolve the annotation to confirm it's androidx.compose.runtime.Composable
    // and not an unrelated annotation with the same short name.

    private fun checkParameter(parameter: KtParameter, holder: ProblemsHolder) {
        val descriptor = TypeDescriptorExtractor.extract(parameter) ?: return
        val stability = StabilityInferencer.infer(descriptor)

        if (stability == Stability.STABLE) return

        val message = when (stability) {
            Stability.UNSTABLE -> "Parameter '${parameter.name}' has an unstable type and may " +
                    "cause unnecessary recompositions. Consider using an immutable type or " +
                    "annotating the class with @Stable/@Immutable."
            Stability.UNKNOWN -> "Parameter '${parameter.name}' has a type whose stability " +
                    "cannot be determined (defined outside this module). Compose will treat it " +
                    "as unstable by default."
            Stability.STABLE -> return
        }

        holder.registerProblem(parameter, message, ProblemHighlightType.WARNING)
    }
}