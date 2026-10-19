package com.example

import com.example.util.TextHistoryManager
import com.example.util.TextImportParser
import com.example.util.UnicodeMathUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testTextHistoryManager_UndoAndRedo() {
        val manager = TextHistoryManager(initialText = "")
        assertFalse(manager.canUndo)
        assertFalse(manager.canRedo)

        manager.push("First Line")
        assertTrue(manager.canUndo)
        assertFalse(manager.canRedo)
        assertEquals("First Line", manager.currentText)

        manager.push("First Line\nSecond Line")
        assertEquals("First Line\nSecond Line", manager.currentText)

        // Step back (Undo)
        val undone1 = manager.undo()
        assertEquals("First Line", undone1)
        assertTrue(manager.canRedo)

        val undone2 = manager.undo()
        assertEquals("", undone2)
        assertFalse(manager.canUndo)
        assertTrue(manager.canRedo)

        // Step forward (Redo)
        val redone1 = manager.redo()
        assertEquals("First Line", redone1)
        assertTrue(manager.canUndo)

        val redone2 = manager.redo()
        assertEquals("First Line\nSecond Line", redone2)
        assertFalse(manager.canRedo)
    }

    @Test
    fun testTextImportParser_DelimitersAndFormulas() {
        val sampleText = """
            [Q] A transmission line has L = 250 nH/m and C = 100 pF/m. Calculate Z_0.
            [A] Z_0 = 50 Ω
            [SOL]
            Step 1: Z_0 = sqrt(L / C)
            Z_0 = sqrt((250 × 10^-9) / (100 × 10^-12)) = 50 Ω
            [FORMULA] Characteristic Impedance: Z_0 = sqrt(L / C) | Lossless line
            [CALCU] Polar: [MODE] [2:CMPLX] [SHIFT] [2] [3] | Canon F-789SGA
        """.trimIndent()

        val result = TextImportParser.parse(sampleText)
        assertEquals(1, result.items.size)

        val item = result.items[0]
        assertTrue(item.stem.contains("Z₀") || item.stem.contains("Z_0"))
        assertTrue(item.answer.contains("50"))
        assertEquals(1, item.formulas.size)
        assertEquals(1, item.calcuTechniques.size)
        assertTrue(item.stemHash.isNotEmpty())
    }

    @Test
    fun testTextImportParser_McqSanitizer() {
        val rawMcq = """
            What semiconductor device exhibits negative resistance in its operating region?
            A) BJT
            B) Tunnel Diode
            C) Zener Diode
            D) JFET
        """.trimIndent()

        val sanitized = TextImportParser.sanitizeMcqStem(rawMcq)
        assertFalse(sanitized.contains("A)"))
        assertFalse(sanitized.contains("B)"))
        assertTrue(sanitized.contains("negative resistance"))
    }

    @Test
    fun testUnicodeMathUtils_GreekAndSuperscripts() {
        val raw = "beta = 100, omega = 2*pi*f, 10^-9, x^2"
        val formatted = UnicodeMathUtils.formatToUnicodeMath(raw)

        assertTrue(formatted.contains("β"))
        assertTrue(formatted.contains("ω"))
        assertTrue(formatted.contains("π"))
        assertTrue(formatted.contains("10⁻⁹"))
        assertTrue(formatted.contains("x²"))
    }
}
