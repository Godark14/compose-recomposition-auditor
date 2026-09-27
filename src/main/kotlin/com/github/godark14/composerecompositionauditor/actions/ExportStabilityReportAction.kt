package com.github.godark14.composerecompositionauditor.actions

import com.github.godark14.composerecompositionauditor.licensing.LicensingUtil
import com.github.godark14.composerecompositionauditor.report.StabilityReportCollector
import com.github.godark14.composerecompositionauditor.report.StabilityReportEntry
import com.github.godark14.composerecompositionauditor.report.StabilityReportExporter
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.runReadActionBlocking
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import java.io.File

class ExportStabilityReportAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        if (LicensingUtil.isPremiumUnlocked() != true) {
            Messages.showInfoMessage(
                project,
                "Exporting stability reports is a Premium feature. Start your 30-day trial from the plugin settings.",
                "Premium Feature"
            )
            return
        }

        ProgressManager.getInstance().run(
            object : Task.Backgroundable(project, "Exporting Stability Report", true) {
                override fun run(indicator: ProgressIndicator) {
                    indicator.isIndeterminate = true
                    indicator.text = "Scanning Kotlin files…"

                    val kotlinFiles = runReadActionBlocking {
                        collectProjectKotlinFiles(project)
                    }

                    if (kotlinFiles.isEmpty()) {
                        showOnEdt(project) {
                            Messages.showInfoMessage(project, "No Kotlin files found to analyze.", "Export")
                        }
                        return
                    }

                    indicator.text = "Analyzing ${kotlinFiles.size} file(s)…"
                    indicator.checkCanceled()

                    val entries = StabilityReportCollector(project).collect(kotlinFiles)

                    indicator.checkCanceled()
                    indicator.text = "Writing report…"

                    val outputDir = project.basePath
                    if (outputDir == null) {
                        showOnEdt(project) {
                            Messages.showErrorDialog(project, "Could not determine project directory.", "Export Failed")
                        }
                        return
                    }

                    writeReport(entries, outputDir)

                    showOnEdt(project) {
                        Messages.showInfoMessage(
                            project,
                            "Report exported to $outputDir\n${entries.size} issue(s) found.",
                            "Export Complete"
                        )
                    }
                }

                override fun onCancel() {
                    showOnEdt(project) {
                        Messages.showInfoMessage(project, "Export cancelled.", "Export")
                    }
                }
            }
        )
    }

    private fun writeReport(entries: List<StabilityReportEntry>, outputDir: String) {
        val exporter = StabilityReportExporter()
        exporter.exportHtml(entries, File(outputDir, "compose-stability-report.html"))
        exporter.exportJson(entries, File(outputDir, "compose-stability-report.json"))
    }

    private fun showOnEdt(project: Project, block: () -> Unit) {
        ApplicationManager.getApplication().invokeLater(block) { project.isDisposed }
    }

    private fun collectProjectKotlinFiles(project: Project): List<VirtualFile> {
        val fileIndex = ProjectFileIndex.getInstance(project)
        val kotlinFileType = FileTypeManager.getInstance().getFileTypeByExtension("kt")
        val result = mutableListOf<VirtualFile>()

        fileIndex.iterateContent { vFile ->
            if (!vFile.isDirectory && vFile.fileType == kotlinFileType) {
                result += vFile
            }
            true
        }
        return result
    }
}