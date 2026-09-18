package com.github.godark14.composerecompositionauditor.stability

import com.github.godark14.composerecompositionauditor.stability.TypeDescriptor.ClassType
import com.github.godark14.composerecompositionauditor.stability.TypeDescriptor.FunctionType
import com.github.godark14.composerecompositionauditor.stability.TypeDescriptor.ImmutableCollectionType
import com.github.godark14.composerecompositionauditor.stability.TypeDescriptor.MutableCollectionType
import com.github.godark14.composerecompositionauditor.stability.TypeDescriptor.PrimitiveType
import org.junit.Assert.assertEquals
import org.junit.Test

class StabilityInferencerTest {

    @Test
    fun `primitive types are stable`() {
        assertEquals(Stability.STABLE, StabilityInferencer.infer(PrimitiveType("Int")))
    }

    @Test
    fun `function types are stable`() {
        assertEquals(Stability.STABLE, StabilityInferencer.infer(FunctionType("() -> Unit")))
    }

    @Test
    fun `mutable collection interfaces are unstable`() {
        assertEquals(Stability.UNSTABLE, StabilityInferencer.infer(MutableCollectionType("List")))
    }

    @Test
    fun `immutable collections are stable`() {
        assertEquals(Stability.STABLE, StabilityInferencer.infer(ImmutableCollectionType("ImmutableList")))
    }

    @Test
    fun `class with var property is unstable`() {
        val type = ClassType(name = "UiState", hasVarProperty = true)
        assertEquals(Stability.UNSTABLE, StabilityInferencer.infer(type))
    }

    @Test
    fun `class annotated Stable is stable even with var property`() {
        val type = ClassType(name = "UiState", isAnnotatedStable = true, hasVarProperty = true)
        assertEquals(Stability.STABLE, StabilityInferencer.infer(type))
    }

    @Test
    fun `class annotated Immutable is stable`() {
        val type = ClassType(name = "UiState", isAnnotatedImmutable = true)
        assertEquals(Stability.STABLE, StabilityInferencer.infer(type))
    }

    @Test
    fun `external class with no visible body is unknown`() {
        val type = ClassType(name = "ThirdPartyModel", isExternal = true)
        assertEquals(Stability.UNKNOWN, StabilityInferencer.infer(type))
    }

    @Test
    fun `plain val-only class is stable`() {
        val type = ClassType(name = "UiState")
        assertEquals(Stability.STABLE, StabilityInferencer.infer(type))
    }

    @Test
    fun `known stable external type is stable`() {
        val type = TypeDescriptor.KnownStableType(name = "Modifier")
        assertEquals(Stability.STABLE, StabilityInferencer.infer(type))
    }
}