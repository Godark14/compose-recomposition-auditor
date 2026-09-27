package com.github.godark14.composerecompositionauditor.report

import java.io.File
import java.time.LocalDateTime

class StabilityReportExporter {

    fun exportJson(entries: List<StabilityReportEntry>, outputFile: File) {
        val json = buildString {
            append("{\n")
            append("  \"generatedAt\": \"${LocalDateTime.now()}\",\n")
            append("  \"totalIssues\": ${entries.size},\n")
            append("  \"issues\": [\n")
            entries.forEachIndexed { index, entry ->
                append("    {\n")
                append("      \"file\": \"${entry.filePath.replace("\\", "\\\\")}\",\n")
                append("      \"function\": \"${entry.functionName}\",\n")
                append("      \"line\": ${entry.lineNumber},\n")
                append("      \"category\": \"${entry.category}\",\n")
                append("      \"description\": \"${entry.description.replace("\"", "\\\"")}\"\n")
                append("    }${if (index < entries.size - 1) "," else ""}\n")
            }
            append("  ]\n")
            append("}\n")
        }
        outputFile.writeText(json)
    }

    fun exportHtml(entries: List<StabilityReportEntry>, outputFile: File) {
        val rows = entries.joinToString("\n") { entry ->
            """
            <tr>
                <td>${entry.filePath}</td>
                <td>${entry.functionName}</td>
                <td>${entry.lineNumber}</td>
                <td>${entry.category}</td>
                <td>${entry.description}</td>
            </tr>
            """.trimIndent()
        }

        val html = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Compose Stability Report</title>
                <style>
                    body { font-family: sans-serif; margin: 2rem; }
                    table { border-collapse: collapse; width: 100%; }
                    th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
                    th { background-color: #f4f4f4; }
                </style>
            </head>
            <body>
                <h1>Compose Stability Report</h1>
                <p>Generated: ${LocalDateTime.now()}</p>
                <p>Total issues: ${entries.size}</p>
                <table>
                    <tr><th>File</th><th>Function</th><th>Line</th><th>Category</th><th>Description</th></tr>
                    $rows
                </table>
            </body>
            </html>
        """.trimIndent()
        outputFile.writeText(html)
    }
}