package com.github.godark14.composerecompositionauditor.stability

import com.github.godark14.composerecompositionauditor.inspection.ComposableUtils.isComposable
import com.github.godark14.composerecompositionauditor.inspection.ComposableUtils.isPreview
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid

object ComposableStabilityAnalyzer {

    fun analyzeFile(file: KtFile): List<StabilityIssue> {
        val issues = mutableListOf<StabilityIssue>()
        file.accept(object : KtTreeVisitorVoid() {
            override fun visitNamedFunction(function: KtNamedFunction) {
                super.visitNamedFunction(function)
                if (!function.isComposable() || function.isPreview()) return
                function.valueParameters.forEach { parameter ->
                    analyzeParameter(parameter, function, file)?.let { issues += it }
                }
            }
        })
        return issues
    }

    fun analyzeParameter(
        parameter: KtParameter,
        function: KtNamedFunction,
        file: KtFile
    ): StabilityIssue? {
        val descriptor = TypeDescriptorExtractor.extract(parameter) ?: return null
        val stability = StabilityInferencer.infer(descriptor)

        if (stability == Stability.STABLE) return null

        val message = when (stability) {
            Stability.UNSTABLE -> "Parameter '${parameter.name}' has an unstable type and may " +
                    "cause unnecessary recompositions. Consider using an immutable type or " +
                    "annotating the class with @Stable/@Immutable."
            Stability.UNKNOWN -> "Parameter '${parameter.name}' has a type whose stability " +
                    "cannot be determined (defined outside this module). Compose will treat it " +
                    "as unstable by default."
            Stability.STABLE -> return null
        }

        val document = file.viewProvider.document
        val lineNumber = document?.getLineNumber(parameter.textOffset)?.plus(1) ?: -1

        return StabilityIssue(
            functionName = function.name ?: "unknown",
            elementName = parameter.name ?: "unknown",
            filePath = file.virtualFile?.path ?: file.name,
            lineNumber = lineNumber,
            category = IssueCategory.UNSTABLE_PARAMETER,
            message = message
        )
    }

    fun descriptorFor(parameter: KtParameter): TypeDescriptor? =
        TypeDescriptorExtractor.extract(parameter)
}