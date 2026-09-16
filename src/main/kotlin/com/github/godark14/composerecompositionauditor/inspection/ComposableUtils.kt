package com.github.godark14.composerecompositionauditor.inspection

import org.jetbrains.kotlin.psi.KtNamedFunction

/**
 * Shared PSI-level checks reused across all inspections in this plugin.
 * Kept as short-name annotation checks (no resolution) for simplicity and
 * speed — a function named e.g. @Preview from an unrelated package would
 * technically slip through, which is an acceptable tradeoff for now.
 */
object ComposableUtils {

    fun KtNamedFunction.isComposable(): Boolean =
        annotationEntries.any { it.shortName?.asString() == "Composable" }

    /** True for @Preview, or the common @PreviewScreenSizes / @PreviewFontScale
     * etc. multipreview annotations, which all end with "Preview". */
    fun KtNamedFunction.isPreview(): Boolean =
        annotationEntries.any { it.shortName?.asString()?.endsWith("Preview") == true }
}