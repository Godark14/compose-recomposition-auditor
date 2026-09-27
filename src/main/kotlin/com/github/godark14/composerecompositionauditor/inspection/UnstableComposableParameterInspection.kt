package com.github.godark14.composerecompositionauditor.inspection

import com.github.godark14.composerecompositionauditor.inspection.ComposableUtils.isComposable
import com.github.godark14.composerecompositionauditor.inspection.ComposableUtils.isPreview
import com.github.godark14.composerecompositionauditor.stability.ComposableStabilityAnalyzer
import com.github.godark14.composerecompositionauditor.stability.TypeDescriptor
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtVisitorVoid

class UnstableComposableParameterInspection : LocalInspectionTool() {

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): KtVisitorVoid =
        object : KtVisitorVoid() {
            override fun visitNamedFunction(function: KtNamedFunction) {
                if (!function.isComposable() || function.isPreview()) return
                val file = function.containingFile as? KtFile ?: return
                function.valueParameters.forEach { parameter ->
                    checkParameter(parameter, function, file, holder)
                }
            }
        }

    private fun checkParameter(
        parameter: KtParameter,
        function: KtNamedFunction,
        file: KtFile,
        holder: ProblemsHolder
    ) {
        val issue = ComposableStabilityAnalyzer.analyzeParameter(parameter, function, file) ?: return
        val descriptor = ComposableStabilityAnalyzer.descriptorFor(parameter) ?: return
        val fixes = buildFixes(descriptor)
        holder.registerProblem(parameter, issue.message, ProblemHighlightType.WARNING, *fixes.toTypedArray())
    }

    private fun buildFixes(descriptor: TypeDescriptor): List<LocalQuickFix> = when (descriptor) {
        is TypeDescriptor.MutableCollectionType ->
            listOf(ConvertToImmutableCollectionQuickFix(descriptor.name))

        is TypeDescriptor.ClassType ->
            if (descriptor.hasVarProperty) listOf(AddStableAnnotationQuickFix()) else emptyList()

        else -> emptyList()
    }
}