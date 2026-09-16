package com.github.godark14.composerecompositionauditor.inspection

import com.github.godark14.composerecompositionauditor.inspection.ComposableUtils.isComposable
import com.github.godark14.composerecompositionauditor.inspection.ComposableUtils.isPreview
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.idea.references.mainReference
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtLambdaArgument
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtVisitorVoid

class UnstableLambdaCaptureInspection : LocalInspectionTool() {

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): KtVisitorVoid =
        object : KtVisitorVoid() {
            override fun visitNamedFunction(function: KtNamedFunction) {
                if (!function.isComposable() || function.isPreview()) return
                val body = function.bodyExpression ?: function.bodyBlockExpression ?: return

                PsiTreeUtil.findChildrenOfType(body, KtLambdaExpression::class.java)
                    .filterNot { it.isDirectArgumentOfRemember() }
                    .forEach { lambda -> checkLambda(lambda, function, holder) }
            }
        }

    private fun KtLambdaExpression.isDirectArgumentOfRemember(): Boolean {
        val lambdaArgument = parent as? KtLambdaArgument ?: return false
        val call = lambdaArgument.parent as? KtCallExpression ?: return false
        return call.calleeExpression?.text == "remember"
    }

    private fun checkLambda(lambda: KtLambdaExpression, function: KtNamedFunction, holder: ProblemsHolder) {
        val flaggedNames = mutableSetOf<String>()

        PsiTreeUtil.findChildrenOfType(lambda, KtNameReferenceExpression::class.java).forEach { reference ->
            val property = reference.mainReference.resolve() as? KtProperty ?: return@forEach

            val isLocalToThisFunction = PsiTreeUtil.isAncestor(function, property, true)
            if (!isLocalToThisFunction) return@forEach
            if (!property.isVar) return@forEach
            if (property.isRememberDelegated()) return@forEach

            val name = property.name ?: return@forEach
            if (!flaggedNames.add(name)) return@forEach

            holder.registerProblem(
                reference,
                "Lambda captures local var '$name' without remember. Compose can't observe " +
                        "changes to a plain captured variable — wrap it with " +
                        "'by remember { mutableStateOf(...) }' so recomposition can track it.",
                ProblemHighlightType.WARNING,
            )
        }
    }

    private fun KtProperty.isRememberDelegated(): Boolean {
        val delegateCall = delegate?.expression as? KtCallExpression ?: return false
        return delegateCall.calleeExpression?.text == "remember"
    }
}