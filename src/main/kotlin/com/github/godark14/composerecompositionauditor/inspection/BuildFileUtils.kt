package com.github.godark14.composerecompositionauditor.inspection

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiManager
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtFile

private val LOG = Logger.getInstance(BuildFileUtils::class.java)

object BuildFileUtils {

    fun addImplementationDependencyIfNeeded(project: Project, fromFile: VirtualFile, coordinateNotation: String) {
        val buildFile = findNearestBuildGradleKts(fromFile)
        if (buildFile == null) {
            LOG.warn("[ComposeAuditor] No build.gradle.kts found walking up from ${fromFile.path}")
            return
        }

        val ktFile = PsiManager.getInstance(project).findFile(buildFile) as? KtFile
        if (ktFile == null) {
            LOG.warn("[ComposeAuditor] build.gradle.kts found but PsiManager couldn't resolve it as KtFile")
            return
        }

        if (ktFile.text.contains(coordinateNotation)) {
            return // already declared, nothing to do
        }

        val dependenciesCall = findDependenciesCall(ktFile)
        if (dependenciesCall == null) {
            LOG.warn("[ComposeAuditor] Could not locate dependencies { } call in build.gradle.kts")
            return
        }

        val document = PsiDocumentManager.getInstance(project).getDocument(ktFile)
        if (document == null) {
            LOG.warn("[ComposeAuditor] Could not get Document for build.gradle.kts")
            return
        }

        // The call's own textRange spans "dependencies { ... }" including both
        // braces (unlike bodyExpression's range, which excludes them — braces
        // are children of the enclosing KtFunctionLiteral, not the block).
        val insertionOffset = dependenciesCall.textRange.endOffset - 1
        document.insertString(insertionOffset, "    implementation(\"$coordinateNotation\")\n")
        PsiDocumentManager.getInstance(project).commitDocument(document)
    }

    private fun findNearestBuildGradleKts(fromFile: VirtualFile): VirtualFile? {
        var current: VirtualFile? = if (fromFile.isDirectory) fromFile else fromFile.parent
        while (current != null) {
            current.findChild("build.gradle.kts")?.let { return it }
            current = current.parent
        }
        return null
    }

    private fun findDependenciesCall(file: KtFile): KtCallExpression? =
        PsiTreeUtil.findChildrenOfType(file, KtCallExpression::class.java)
            .firstOrNull { it.calleeExpression?.text == "dependencies" }
}