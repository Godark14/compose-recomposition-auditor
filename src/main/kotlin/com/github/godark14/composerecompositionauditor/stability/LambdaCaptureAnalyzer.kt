package com.github.godark14.composerecompositionauditor.stability

import com.github.godark14.composerecompositionauditor.inspection.ComposableUtils.isComposable
import com.github.godark14.composerecompositionauditor.inspection.ComposableUtils.isPreview
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.idea.references.mainReference
import org.jetbrains.kotlin.psi.*

object LambdaCaptureAnalyzer {

    fun analyzeFile(file: KtFile): List<StabilityIssue> {
        val issues = mutableListOf<StabilityIssue>()
        file.accept(object : KtTreeVisitorVoid() {
            override fun visitNamedFunction(function: KtNamedFunction) {
                super.visitNamedFunction(function)
                if (!function.isComposable() || function.isPreview()) return
                issues += analyzeFunction(function, file)
            }
        })
        return issues
    }

    fun analyzeFunction(function: KtNamedFunction, file: KtFile): List<StabilityIssue> {
        val body = function.bodyExpression ?: function.bodyBlockExpression ?: return emptyList()
        val issues = mutableListOf<StabilityIssue>()

        PsiTreeUtil.findChildrenOfType(body, KtLambdaExpression::class.java)
            .filterNot { isDirectArgumentOfRemember(it) }
            .forEach { lambda -> issues += analyzeLambda(lambda, function, file) }

        return issues
    }

    private fun analyzeLambda(
        lambda: KtLambdaExpression,
        function: KtNamedFunction,
        file: KtFile
    ): List<StabilityIssue> {
        val issues = mutableListOf<StabilityIssue>()
        val flaggedNames = mutableSetOf<String>()
        val document = file.viewProvider.document

        PsiTreeUtil.findChildrenOfType(lambda, KtNameReferenceExpression::class.java).forEach { reference ->
            val property = reference.mainReference.resolve() as? KtProperty ?: return@forEach

            val isLocalToThisFunction = PsiTreeUtil.isAncestor(function, property, true)
            if (!isLocalToThisFunction) return@forEach
            if (!property.isVar) return@forEach
            if (property.isRememberDelegated()) return@forEach

            val name = property.name ?: return@forEach
            if (!flaggedNames.add(name)) return@forEach

            val lineNumber = document?.getLineNumber(reference.textOffset)?.plus(1) ?: -1

            issues += StabilityIssue(
                functionName = function.name ?: "unknown",
                elementName = name,
                filePath = file.virtualFile?.path ?: file.name,
                lineNumber = lineNumber,
                category = IssueCategory.UNSTABLE_LAMBDA_CAPTURE,
                message = "Lambda captures local var '$name' without remember. Compose can't observe " +
                        "changes to a plain captured variable — wrap it with " +
                        "'by remember { mutableStateOf(...) }' so recomposition can track it."
            )
        }
        return issues
    }

    // Fonction top-level unique — exposée pour que l'inspection réutilise le même filtre
    fun isDirectArgumentOfRemember(lambda: KtLambdaExpression): Boolean {
        val lambdaArgument = lambda.parent as? KtLambdaArgument ?: return false
        val call = lambdaArgument.parent as? KtCallExpression ?: return false
        return call.calleeExpression?.text == "remember"
    }

    private fun KtProperty.isRememberDelegated(): Boolean {
        val delegateCall = delegate?.expression as? KtCallExpression ?: return false
        return delegateCall.calleeExpression?.text == "remember"
    }
}