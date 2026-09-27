package com.github.godark14.composerecompositionauditor.report

import com.github.godark14.composerecompositionauditor.stability.ComposableStabilityAnalyzer
import com.github.godark14.composerecompositionauditor.stability.LambdaCaptureAnalyzer
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.runReadActionBlocking
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import org.jetbrains.kotlin.psi.KtFile
import java.util.concurrent.ExecutionException

class StabilityReportCollector(private val project: Project) {

    fun collect(kotlinFiles: List<VirtualFile>): List<StabilityReportEntry> {
        val future = ApplicationManager.getApplication().executeOnPooledThread<List<StabilityReportEntry>> {
            runReadActionBlocking {
                val psiManager = PsiManager.getInstance(project)
                kotlinFiles.flatMap { vFile ->
                    val ktFile = psiManager.findFile(vFile) as? KtFile ?: return@flatMap emptyList()

                    val parameterIssues = ComposableStabilityAnalyzer.analyzeFile(ktFile)
                    val lambdaIssues = LambdaCaptureAnalyzer.analyzeFile(ktFile)

                    (parameterIssues + lambdaIssues).map { issue ->
                        StabilityReportEntry(
                            filePath = issue.filePath,
                            functionName = issue.functionName,
                            lineNumber = issue.lineNumber,
                            category = issue.category,
                            description = issue.message,
                            severity = "WARNING"
                        )
                    }
                }
            }
        }

        return try {
            future.get()
        } catch (e: ExecutionException) {
            throw e.cause ?: e
        }
    }
}