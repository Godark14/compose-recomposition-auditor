package com.github.godark14.composerecompositionauditor.inspection

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.idea.references.mainReference
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.KtUserType

private const val STABLE_ANNOTATION_FQN = "androidx.compose.runtime.Stable"

class AddStableAnnotationQuickFix : LocalQuickFix {

    override fun getFamilyName(): String = "Annotate class with @Stable"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val parameter = descriptor.psiElement as? KtParameter ?: return
        val classDeclaration = resolveParameterClassDeclaration(parameter) ?: return

        if (classDeclaration.annotationEntries.any { it.shortName?.asString() == "Stable" }) {
            return // already annotated, nothing to do
        }

        val factory = KtPsiFactory(project)
        val annotationEntry = factory.createAnnotationEntry("@Stable")
        classDeclaration.addAnnotationEntry(annotationEntry)

        val file = classDeclaration.containingFile as? KtFile ?: return
        ImportUtils.addImportIfNeeded(file, STABLE_ANNOTATION_FQN)
    }

    /**
     * Resolves the class declaration behind the parameter's type using
     * standard PSI reference resolution (KtReference.resolve()), which
     * works identically under K1 and K2 — avoids relying on Analysis API
     * symbol.psi, which returned null/unattached PSI in testing.
     */
    private fun resolveParameterClassDeclaration(parameter: KtParameter): KtClassOrObject? {
        val userType = parameter.typeReference?.typeElement as? KtUserType ?: return null
        val referenceExpression = userType.referenceExpression ?: return null
        return referenceExpression.mainReference.resolve() as? KtClassOrObject
    }
}