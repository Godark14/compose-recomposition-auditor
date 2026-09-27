package com.github.godark14.composerecompositionauditor.stability

enum class IssueCategory {
    UNSTABLE_PARAMETER,
    UNSTABLE_LAMBDA_CAPTURE
}

data class StabilityIssue(
    val functionName: String,
    val elementName: String,
    val filePath: String,
    val lineNumber: Int,
    val category: IssueCategory,
    val message: String
)