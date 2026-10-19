package com.example

import android.content.Context
import androidx.compose.ui.unit.sp
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.SampleData
import com.example.data.model.FormulaEntity
import com.example.data.model.ImportBatchEntity
import com.example.data.model.QuestionEntity
import com.example.data.model.SubjectEntity
import com.example.util.AiPromptTemplate
import com.example.util.TextImportParser
import com.example.util.UnicodeMathUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("ECE FlashMaster", appName)
    }

    @Test
    fun `unicode math utils formats muA and all sample questions cleanly`() {
        val formatted = UnicodeMathUtils.formatToAnnotatedMath(
            "[IEQ]I_B = 25\\text{ }\\mu\\text{A}[/IEQ]",
            16.sp
        ).text
        assertTrue("Expected 25 μA but got: $formatted", formatted.contains("25 μA"))
        assertFalse("Should not contain raw \\mu", formatted.contains("\\mu"))

        SampleData.getInitialQuestions().forEach { q ->
            val stemText = UnicodeMathUtils.formatToAnnotatedMath(q.stem, 16.sp).text
            val ansText = UnicodeMathUtils.formatToAnnotatedMath(q.answer, 16.sp).text
            assertFalse(stemText.contains("\\mu"))
            assertFalse(ansText.contains("\\text"))
        }
    }

    @Test
    fun `ai prompt template enforces 1to1 questions, zero duplicates, and concept separation`() {
        val prompt = AiPromptTemplate.PROMPT_TEXT
        assertTrue("Must require 1:1 question preservation", prompt.contains("1:1"))
        assertTrue("Must prohibit paraphrasing/rewriting", prompt.contains("Do NOT rewrite, paraphrase, simplify"))
        assertTrue("Must enforce duplicate avoidance", prompt.contains("AVOID DUPLICATES"))
        assertTrue("Must instruct omitting SOL on concepts", prompt.contains("DO NOT output [SOL]"))
        assertTrue("Must instruct omitting FORMULA on concepts", prompt.contains("DO NOT output [FORMULA]"))
        assertTrue("Must instruct omitting CALCU on concepts", prompt.contains("DO NOT output [CALCU]"))
    }

    @Test
    fun `parser preserves parenthetical units in question stem 1to1`() {
        val rawStem = "Determine the collector current (in mA) of the BJT transistor."
        val sanitized = TextImportParser.sanitizeMcqStem(rawStem)
        assertEquals("Should preserve (in mA) without truncation", rawStem, sanitized)

        val rawStemWithUnits = "Calculate the output power (in W) and phase angle (in deg)."
        val sanitized2 = TextImportParser.sanitizeMcqStem(rawStemWithUnits)
        assertEquals("Should preserve units in parentheses", rawStemWithUnits, sanitized2)
    }

    @Test
    fun `parser strips multiple choice options block but keeps question stem 1to1`() {
        val rawStemWithMcq = """
            What is the Nyquist rate for a signal with bandwidth 4 kHz?
            A. 4 kHz
            B. 8 kHz
            C. 16 kHz
            D. 2 kHz
        """.trimIndent()

        val sanitized = TextImportParser.sanitizeMcqStem(rawStemWithMcq)
        assertEquals(
            "What is the Nyquist rate for a signal with bandwidth 4 kHz?",
            sanitized
        )
    }

    @Test
    fun `parser deduplicates identical questions in batch import`() {
        val duplicateText = """
            [Q] What is the efficiency of a Class A amplifier with resistive load?
            [A] Maximum 25%

            [Q] What is the efficiency of a Class A amplifier with resistive load?
            [A] Maximum 25%

            [Q] What is the characteristic impedance of free space?
            [A] 377 Ω (or 120π Ω)
        """.trimIndent()

        val result = TextImportParser.parse(duplicateText)
        assertEquals(2, result.items.size)
        assertEquals(1, result.duplicateCount)
        assertEquals(
            "What is the efficiency of a Class A amplifier with resistive load?",
            result.items[0].stem
        )
        assertEquals(
            "What is the characteristic impedance of free space?",
            result.items[1].stem
        )
    }

    @Test
    fun `parser cleanly parses concept questions with no sol, formula, or calcu`() {
        val conceptText = """
            [Q] State Barkhausen criterion for oscillation.
            [A] Loop gain magnitude |Aβ| = 1 and phase shift = 0° (or 360°).
        """.trimIndent()

        val result = TextImportParser.parse(conceptText)
        assertEquals(1, result.items.size)
        val card = result.items[0]
        assertEquals("State Barkhausen criterion for oscillation.", card.stem)
        assertEquals("Loop gain magnitude |Aβ| = 1 and phase shift = 0° (or 360°).", card.answer)
        assertTrue("Solution should be empty for concept question", card.solution.isEmpty())
        assertTrue("Formulas should be empty for concept question", card.formulas.isEmpty())
        assertTrue("Techniques should be empty for concept question", card.calcuTechniques.isEmpty())
    }

    @Test
    fun `unicode math utils formats circuit operators like parallel and logic symbols`() {
        val parallelMath = UnicodeMathUtils.formatToUnicodeMath("Z_{in} = R_1 \\parallel R_2")
        assertTrue("Should contain parallel symbol ∥: $parallelMath", parallelMath.contains("∥"))

        val logicMath = UnicodeMathUtils.formatToUnicodeMath("\\therefore V = 5\\text{ V} \\implies I = 2\\text{ A}")
        assertTrue("Should contain therefore ∴: $logicMath", logicMath.contains("∴"))
        assertTrue("Should contain implies ⇒: $logicMath", logicMath.contains("⇒"))
    }

    @Test
    fun `unicode math utils formats complex nested fractions without leaving raw frac`() {
        // Nested fraction with radical in denominator
        val resFreq = UnicodeMathUtils.formatToUnicodeMath("f_r = \\frac{1}{2\\pi \\sqrt{LC}}")
        assertFalse("Should not have raw \\frac: $resFreq", resFreq.contains("\\frac"))
        assertTrue("Should contain 1 / 2π √(LC): $resFreq", resFreq.contains("(1 / 2π √(LC))"))

        // Fraction with subscript in numerator
        val voltageDiv = UnicodeMathUtils.formatToUnicodeMath("V_{out} = \\frac{V_{in} \\cdot R_2}{R_1 + R_2}")
        assertFalse("Should not have raw \\frac: $voltageDiv", voltageDiv.contains("\\frac"))
        assertTrue("Should have proper fraction structure: $voltageDiv", voltageDiv.contains(" / "))

        // dfrac and tfrac support
        val dfrac = UnicodeMathUtils.formatToUnicodeMath("\\dfrac{d^2 x}{dt^2} + \\omega_0^2 x = 0")
        assertFalse("Should not have raw \\dfrac: $dfrac", dfrac.contains("\\dfrac"))
    }

    @Test
    fun `unicode math utils formats radicals with inner braces without truncation`() {
        val radWithExp = UnicodeMathUtils.formatToUnicodeMath("\\sqrt{100 \\times 10^{-6}}")
        assertFalse("Should not have raw \\sqrt: $radWithExp", radWithExp.contains("\\sqrt"))
        assertFalse("Should not have dangling unmatched brace: $radWithExp", radWithExp.contains("}"))
        assertTrue("Should contain root notation: $radWithExp", radWithExp.contains("√("))

        val radImpedance = UnicodeMathUtils.formatToUnicodeMath("Z = \\sqrt{R^2 + (X_L - X_C)^2}")
        assertFalse(radImpedance.contains("\\sqrt"))
        assertFalse(radImpedance.contains("}"))
        assertTrue(radImpedance.contains("√("))
    }

    @Test
    fun `unicode math utils formats all engineering micro units cleanly`() {
        val units = listOf(
            "\\muA" to "μA",
            "\\muF" to "μF",
            "\\muH" to "μH",
            "\\muV" to "μV",
            "\\mus" to "μs",
            "\\mum" to "μm",
            "\\muW" to "μW",
            "\\muC" to "μC",
            "\\mu\\Omega" to "μΩ",
            "100\\muA" to "100μA"
        )
        for ((input, expected) in units) {
            val formatted = UnicodeMathUtils.formatToUnicodeMath(input)
            assertTrue("Expected $expected for input $input but got $formatted", formatted.contains(expected))
            assertFalse("Should not contain raw \\mu in $formatted", formatted.contains("\\mu"))
        }
    }

    @Test
    fun `unicode math utils strips LaTeX display and inline math delimiters`() {
        val displayMath = UnicodeMathUtils.formatToUnicodeMath("\\[ f = \\frac{1}{T} \\]")
        assertFalse("Should not contain \\[: $displayMath", displayMath.contains("\\["))
        assertFalse("Should not contain \\]: $displayMath", displayMath.contains("\\]"))
        assertFalse("Should not contain \\frac: $displayMath", displayMath.contains("\\frac"))

        val inlineDollar = UnicodeMathUtils.formatToUnicodeMath("When \$I_C = 10\\text{ mA}\$ and \$V_{CE} = 5\\text{ V}\$")
        assertFalse("Should not contain $: $inlineDollar", inlineDollar.contains("$"))
        assertFalse("Should not contain \\text: $inlineDollar", inlineDollar.contains("\\text"))
    }

    @Test
    fun `unicode math utils preserves arrows and never leaves orphaned arrow token`() {
        val arrowText = UnicodeMathUtils.formatToUnicodeMath("As x \\rightarrow \\infty and y \\leftarrow 0, \\implies z \\to 1")
        assertTrue("Should contain right arrow →: $arrowText", arrowText.contains("→"))
        assertTrue("Should contain left arrow ←: $arrowText", arrowText.contains("←"))
        assertTrue("Should contain implies ⇒: $arrowText", arrowText.contains("⇒"))
        assertFalse("Must NOT leave orphaned 'arrow' token: $arrowText", arrowText.contains("arrow"))
    }

    @Test
    fun `unicode math utils strips backslash from math functions and handles inequality relations`() {
        val trig = UnicodeMathUtils.formatToUnicodeMath("v(t) = V_m \\sin(2\\pi f t) + \\cos(\\theta)")
        assertFalse("Should not contain \\sin: $trig", trig.contains("\\sin"))
        assertFalse("Should not contain \\cos: $trig", trig.contains("\\cos"))
        assertTrue("Should contain clean function sin: $trig", trig.contains("sin(2π"))

        val ineq = UnicodeMathUtils.formatToUnicodeMath("x \\ne 0 and y \\le 5 and z \\ge 10")
        assertTrue("Should contain ≠: $ineq", ineq.contains("≠"))
        assertTrue("Should contain ≤: $ineq", ineq.contains("≤"))
        assertTrue("Should contain ≥: $ineq", ineq.contains("≥"))
    }

    @Test
    fun `unicode math utils cleans cases and aligned environments`() {
        val casesStr = UnicodeMathUtils.formatToUnicodeMath("\\begin{cases} x \\ge 0 \\\\ x < 0 \\end{cases}")
        assertFalse("Should not contain begin{cases}: $casesStr", casesStr.contains("begin{cases}"))
        assertFalse("Should not contain end{cases}: $casesStr", casesStr.contains("end{cases}"))
        assertTrue("Should contain ≥: $casesStr", casesStr.contains("≥"))
    }

    @Test
    fun `text import parser handles standard AI LaTeX dollar delimiters in formulas and stems`() {
        val text = """
            [Q] What is resonant frequency?
            [A] ${'$'}f_r = \frac{1}{2\pi\sqrt{LC}}${'$'}
            [FORMULA] Resonant Frequency: $${'$'}f_r = \frac{1}{2\pi\sqrt{LC}}$${'$'} | L = Inductance, C = Capacitance
        """.trimIndent()
        val parsed = TextImportParser.parse(text)
        assertEquals(1, parsed.items.size)
        assertEquals(1, parsed.items[0].formulas.size)
        assertEquals("f_r = \\frac{1}{2\\pi\\sqrt{LC}}", parsed.items[0].formulas[0].formula)
        assertEquals("Resonant Frequency", parsed.items[0].formulas[0].name)
    }

    @Test
    fun `import batch deletion cascades and removes all questions and formulas in batch`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = AppDatabase.getDatabase(context)
        val subjectId = db.subjectDao().insertSubject(
            SubjectEntity(name = "Batch Test Sub", code = "BTS", description = "Test", colorHex = "#FF5500")
        )
        val batchId = db.importBatchDao().insertBatch(
            ImportBatchEntity(subjectId = subjectId, rawText = "Sample note text", questionCount = 2, formulaCount = 1, calcuCount = 0)
        )
        val q1 = db.questionDao().insertQuestion(
            QuestionEntity(subjectId = subjectId, batchId = batchId, stem = "Test Q1", stemHash = "hash1", answer = "Ans1")
        )
        val q2 = db.questionDao().insertQuestion(
            QuestionEntity(subjectId = subjectId, batchId = batchId, stem = "Test Q2", stemHash = "hash2", answer = "Ans2")
        )
        db.formulaDao().insertFormula(
            FormulaEntity(subjectId = subjectId, questionId = q1, name = "F1", formula = "x=1", hash = "fhash1")
        )

        // Verify inserted
        val questionsBefore = db.questionDao().getQuestionsBySubject(subjectId).first()
        assertTrue(questionsBefore.any { it.id == q1 })
        assertTrue(questionsBefore.any { it.id == q2 })

        // Delete batch
        db.importBatchDao().deleteBatchById(batchId)

        // Verify cascade deletion
        val questionsAfter = db.questionDao().getQuestionsBySubject(subjectId).first()
        assertFalse(questionsAfter.any { it.batchId == batchId })
        val formulaAfter = db.formulaDao().getFormulasForQuestion(q1).first()
        assertTrue(formulaAfter.isEmpty())
    }

    @Test
    fun `individual question deletion removes question and its formulas`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = AppDatabase.getDatabase(context)
        val subjectId = db.subjectDao().insertSubject(
            SubjectEntity(name = "Single Del Sub", code = "SDS", description = "Test", colorHex = "#00FF55")
        )
        val qId = db.questionDao().insertQuestion(
            QuestionEntity(subjectId = subjectId, stem = "Single Q", stemHash = "singlehash", answer = "Single Ans")
        )
        db.formulaDao().insertFormula(
            FormulaEntity(subjectId = subjectId, questionId = qId, name = "F_Single", formula = "y=2", hash = "singlefhash")
        )

        // Delete question
        db.questionDao().deleteQuestionById(qId)

        val qCheck = db.questionDao().getQuestionByIdOnce(qId)
        assertNull(qCheck)
        val fCheck = db.formulaDao().getFormulasForQuestion(qId).first()
        assertTrue(fCheck.isEmpty())
    }
}
