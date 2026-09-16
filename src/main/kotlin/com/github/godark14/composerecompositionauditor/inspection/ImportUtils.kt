package com.github.godark14.composerecompositionauditor.inspection


import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtImportDirective
import org.jetbrains.kotlin.psi.KtPsiFactory

/**
 * Minimal, version-stable import insertion that doesn't rely on Kotlin
 * plugin resolution helpers (those have proven fragile across K1/K2 and
 * Analysis API versions in this project). Pure PSI manipulation only.
 */
object ImportUtils {

    fun addImportIfNeeded(file: KtFile, fqName: String) {
        val alreadyImported = file.importDirectives.any { directive ->
            directive.importedFqName?.asString() == fqName
        }
        if (alreadyImported) return

        val packageOfFqName = fqName.substringBeforeLast('.', missingDelimiterValue = "")
        val samePackageAsFile = packageOfFqName == file.packageFqName.asString()
        if (samePackageAsFile) return

        val factory = KtPsiFactory(file.project)
        val newImport: KtImportDirective = factory.createImportDirective(
            org.jetbrains.kotlin.resolve.ImportPath(FqName(fqName), isAllUnder = false),
        )

        val importList = file.importList
        if (importList != null) {
            importList.add(newImport)
        } else {
            // No import list yet (e.g. file with only a package directive) —
            // insert one right after the package directive, or at the top.
            val anchor = file.packageDirective ?: file.firstChild
            file.addAfter(newImport, anchor)
        }
    }
}