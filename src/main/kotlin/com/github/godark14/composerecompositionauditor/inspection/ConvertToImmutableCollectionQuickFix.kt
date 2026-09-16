package com.github.godark14.composerecompositionauditor.inspection

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.KtTypeReference

private val COLLECTION_TO_IMMUTABLE = mapOf(
    "List" to Triple("ImmutableList", "kotlinx.collections.immutable.ImmutableList", "org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.7"),
    "MutableList" to Triple("ImmutableList", "kotlinx.collections.immutable.ImmutableList", "org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.7"),
    "Map" to Triple("ImmutableMap", "kotlinx.collections.immutable.ImmutableMap", "org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.7"),
    "MutableMap" to Triple("ImmutableMap", "kotlinx.collections.immutable.ImmutableMap", "org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.7"),
    "Set" to Triple("ImmutableSet", "kotlinx.collections.immutable.ImmutableSet", "org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.7"),
    "MutableSet" to Triple("ImmutableSet", "kotlinx.collections.immutable.ImmutableSet", "org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.7"),
)

class ConvertToImmutableCollectionQuickFix(private val sourceTypeName: String) : LocalQuickFix {

    override fun getFamilyName(): String =
        "Convert to ${COLLECTION_TO_IMMUTABLE[sourceTypeName]?.first ?: "immutable collection"}"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val parameter = descriptor.psiElement as? KtParameter ?: return
        val typeReference: KtTypeReference = parameter.typeReference ?: return
        val (immutableName, immutableFqn, gradleCoordinate) = COLLECTION_TO_IMMUTABLE[sourceTypeName] ?: return

        // Preserve generic arguments: "List<String>" -> "ImmutableList<String>"
        val originalText = typeReference.text
        val genericArgsStart = originalText.indexOf('<')
        val genericArgs = if (genericArgsStart >= 0) originalText.substring(genericArgsStart) else ""
        val newTypeText = immutableName + genericArgs

        val factory = KtPsiFactory(project)
        val newTypeReference = factory.createType(newTypeText)
        typeReference.replace(newTypeReference)

        val file = parameter.containingFile as? KtFile ?: return
        ImportUtils.addImportIfNeeded(file, immutableFqn)

        val virtualFile = file.virtualFile ?: return
        BuildFileUtils.addImplementationDependencyIfNeeded(project, virtualFile, gradleCoordinate)
    }
}