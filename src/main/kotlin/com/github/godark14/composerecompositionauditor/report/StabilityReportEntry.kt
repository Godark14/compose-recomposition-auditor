package com.github.godark14.composerecompositionauditor.report

import com.github.godark14.composerecompositionauditor.stability.IssueCategory

data class StabilityReportEntry(
    val filePath: String,
    val functionName: String,
    val lineNumber: Int,
    val category: IssueCategory,
    val description: String,
    val severity: String
)