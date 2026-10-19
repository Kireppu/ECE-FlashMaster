package com.example.data

import com.example.data.model.CalcuTechniqueEntity
import com.example.data.model.FormulaEntity
import com.example.data.model.QuestionEntity
import com.example.data.model.SubjectEntity
import com.example.util.TextImportParser

object SampleData {

    val INITIAL_SUBJECTS = listOf(
        SubjectEntity(
            id = 1,
            name = "Electronics 1 (Devices & Circuits)",
            code = "ELEC-1",
            description = "Diodes, BJT, FETs, Op-Amps, DC Biasing, Small-Signal Analysis",
            colorHex = "#0284C7"
        ),
        SubjectEntity(
            id = 2,
            name = "Communications 2 (Transmission & RF)",
            code = "COMM-2",
            description = "Transmission Lines, Smith Chart, VSWR, Antennas, Radar, Modulation",
            colorHex = "#7C3AED"
        ),
        SubjectEntity(
            id = 3,
            name = "Mathematics & Signals",
            code = "MATH",
            description = "Complex Numbers, Differential Equations, Laplace, Fourier, Linear Algebra",
            colorHex = "#059669"
        ),
        SubjectEntity(
            id = 4,
            name = "GEAS (Engineering Sciences)",
            code = "GEAS",
            description = "Engineering Economics, Thermodynamics, Strength of Materials, Physics",
            colorHex = "#D97706"
        )
    )

    fun getInitialQuestions(): List<QuestionEntity> {
        val q1 = """
A silicon NPN bipolar junction transistor has [IEQ]\beta = 150[/IEQ] and base current [IEQ]I_B = 25\text{ }\mu\text{A}[/IEQ]. If [IEQ]V_{CC} = 15\text{ V}[/IEQ], [IEQ]R_C = 2.4\text{ k}\Omega[/IEQ], and [IEQ]V_{BE} = 0.7\text{ V}[/IEQ]:
1. Determine the collector current [IEQ]I_C[/IEQ].
2. Determine the collector-to-emitter voltage [IEQ]V_{CE}[/IEQ].
3. Verify if the BJT operates in the active region.
""".trimIndent()

        val sol1 = """
Step 1: Calculate Collector Current [IEQ]I_C[/IEQ]:
[EQ]I_C = \beta \cdot I_B[/EQ]
[EQ]I_C = 150 \cdot (25 \times 10^{-6}\text{ A}) = 3.75\text{ mA}[/EQ]

Step 2: Apply KVL around the output collector-emitter loop:
[EQ]V_{CC} - (I_C \cdot R_C) - V_{CE} = 0[/EQ]
[EQ]V_{CE} = V_{CC} - (I_C \cdot R_C)[/EQ]
[EQ]V_{CE} = 15\text{ V} - (3.75\text{ mA} \cdot 2.4\text{ k}\Omega) = 15\text{ V} - 9.0\text{ V} = 6.0\text{ V}[/EQ]

Step 3: Region Verification:
Since [IEQ]V_{CE} = 6.0\text{ V} > V_{CE(sat)} \approx 0.2\text{ V}[/IEQ] and [IEQ]V_{BE} = 0.7\text{ V}[/IEQ], the transistor is confirmed in the Active Region.
""".trimIndent()

        val q2 = """
A lossless coaxial transmission line has distributed inductance [IEQ]L = 250\text{ nH/m}[/IEQ] and distributed capacitance [IEQ]C = 100\text{ pF/m}[/IEQ].
1. Calculate the characteristic impedance [IEQ]Z_0[/IEQ] of the line.
2. If terminated in [IEQ]Z_L = 75 + j50\text{ }\Omega[/IEQ], calculate the magnitude of the reflection coefficient [IEQ]|\Gamma|[/IEQ].
3. Compute the Voltage Standing Wave Ratio (VSWR).
""".trimIndent()

        val sol2 = """
Step 1: Characteristic Impedance [IEQ]Z_0[/IEQ] for a lossless line:
[EQ]Z_0 = \sqrt{\frac{L}{C}}[/EQ]
[EQ]Z_0 = \sqrt{\frac{250 \times 10^{-9}\text{ H/m}}{100 \times 10^{-12}\text{ F/m}}} = \sqrt{2500} = 50.0\text{ }\Omega[/EQ]

Step 2: Reflection Coefficient [IEQ]\Gamma[/IEQ]:
[EQ]\Gamma = \frac{Z_L - Z_0}{Z_L + Z_0}[/EQ]
[EQ]\Gamma = \frac{(75 + j50) - 50}{(75 + j50) + 50} = \frac{25 + j50}{125 + j50}[/EQ]
Converting to polar form:
[EQ]|\Gamma| = \frac{55.90\angle 63.43^\circ}{134.63\angle 21.80^\circ} = 0.415\angle 41.63^\circ[/EQ]

Step 3: Voltage Standing Wave Ratio:
[EQ]\text{VSWR} = \frac{1 + |\Gamma|}{1 - |\Gamma|} = \frac{1 + 0.415}{1 - 0.415} = \frac{1.415}{0.585} = 2.42[/EQ]
""".trimIndent()

        val q3 = """
Given an alternating current circuit with two parallel branches:
Branch 1 has impedance [IEQ]Z_1 = 12 + j16\text{ }\Omega[/IEQ].
Branch 2 has impedance [IEQ]Z_2 = 8 - j6\text{ }\Omega[/IEQ].
Find the total equivalent impedance [IEQ]Z_{eq}[/IEQ] in polar form (magnitude and angle in degrees).
""".trimIndent()

        val sol3 = """
Step 1: Formula for two parallel impedances:
[EQ]Z_{eq} = \frac{Z_1 \cdot Z_2}{Z_1 + Z_2}[/EQ]

Step 2: Add impedances for the denominator:
[EQ]Z_1 + Z_2 = (12 + j16) + (8 - j6) = 20 + j10\text{ }\Omega[/EQ]

Step 3: Direct evaluation via Canon F-789SGA CMPLX Mode:
Enter: [EQ]\frac{(12 + 16i) \cdot (8 - 6i)}{20 + 10i}[/EQ]
Display gives rectangular: [IEQ]7.2 + j1.6\text{ }\Omega[/IEQ]
Press [APPS] [1:▶r∠θ] [=]
[EQ]Z_{eq} = 7.37\angle 12.53^\circ\text{ }\Omega[/EQ]
""".trimIndent()

        val q4 = """
An equipment costs ₱500,000 to purchase and has an estimated life of 10 years with a salvage value of ₱50,000.
Using the Sinking Fund Method of depreciation with annual interest rate [IEQ]i = 8\%[/IEQ], calculate:
1. The annual depreciation charge [IEQ]d[/IEQ].
2. The book value [IEQ]BV_5[/IEQ] at the end of the 5th year.
""".trimIndent()

        val sol4 = """
Step 1: Total depreciable amount:
[EQ]C - C_L = 500,000 - 50,000 = ₱450,000[/EQ]

Step 2: Sinking Fund factor for [IEQ]n = 10, i = 0.08[/IEQ]:
[EQ]\frac{A}{F} = \frac{i}{(1 + i)^n - 1} = \frac{0.08}{(1.08)^{10} - 1} = 0.0690295[/EQ]

Step 3: Annual depreciation charge [IEQ]d[/IEQ]:
[EQ]d = 450,000 \times 0.0690295 = ₱31,063.27\text{ / year}[/EQ]

Step 4: Book Value at year 5:
[EQ]BV_5 = C - D_5 = 500,000 - 182,236.80 = ₱317,763.20[/EQ]
""".trimIndent()

        return listOf(
            QuestionEntity(
                id = 1,
                subjectId = 1,
                stem = q1,
                stemHash = TextImportParser.sha256(q1.lowercase().replace(Regex("\\s+"), " ")),
                answer = """[IEQ]I_C = 3.75\text{ mA}[/IEQ], [IEQ]V_{CE} = 6.0\text{ V}[/IEQ] (Active Region)""",
                solution = sol1,
                isMastered = false
            ),
            QuestionEntity(
                id = 2,
                subjectId = 2,
                stem = q2,
                stemHash = TextImportParser.sha256(q2.lowercase().replace(Regex("\\s+"), " ")),
                answer = """[IEQ]Z_0 = 50.0\text{ }\Omega[/IEQ], [IEQ]|\Gamma| = 0.415[/IEQ], [IEQ]\text{VSWR} = 2.42[/IEQ]""",
                solution = sol2,
                isMastered = false
            ),
            QuestionEntity(
                id = 3,
                subjectId = 3,
                stem = q3,
                stemHash = TextImportParser.sha256(q3.lowercase().replace(Regex("\\s+"), " ")),
                answer = """[IEQ]Z_{eq} = 7.37\angle 12.53^\circ\text{ }\Omega[/IEQ] (or [IEQ]7.2 + j1.6\text{ }\Omega[/IEQ])""",
                solution = sol3,
                isMastered = false
            ),
            QuestionEntity(
                id = 4,
                subjectId = 4,
                stem = q4,
                stemHash = TextImportParser.sha256(q4.lowercase().replace(Regex("\\s+"), " ")),
                answer = """[IEQ]d = ₱31,063.27[/IEQ], [IEQ]BV_5 = ₱317,763.20[/IEQ]""",
                solution = sol4,
                isMastered = false
            )
        )
    }

    fun getInitialFormulas(): List<FormulaEntity> {
        return listOf(
            FormulaEntity(
                id = 1,
                subjectId = 3,
                questionId = null,
                name = "Word 2D Built-Up Demo Equation",
                formula = "\\sum \\frac{a_c^b \\sqrt{133424234232}}{v \\int \\check{c}}",
                variables = "Word-Grade 2D Professional Equation Editor (stacked fraction, radical, sum & integral)",
                hash = TextImportParser.sha256("word_2d_builtup_demo")
            ),
            FormulaEntity(
                id = 2,
                subjectId = 1,
                questionId = 1,
                name = "BJT Collector Current",
                formula = "I_C = \\beta \\cdot I_B",
                variables = "\\beta = \\text{DC Current Gain}, I_B = \\text{Base Current (A)}",
                hash = TextImportParser.sha256("I_c = β · I_b")
            ),
            FormulaEntity(
                id = 3,
                subjectId = 1,
                questionId = 1,
                name = "Common Emitter KVL Output",
                formula = "V_{CE} = V_{CC} - (I_C \\cdot R_C)",
                variables = "V_{CC} = \\text{DC supply}, R_C = \\text{collector resistor}",
                hash = TextImportParser.sha256("V_ce = V_cc - (I_c · R_c)")
            ),
            FormulaEntity(
                id = 4,
                subjectId = 2,
                questionId = 2,
                name = "Lossless Line Characteristic Impedance",
                formula = "Z_0 = \\sqrt{\\frac{L}{C}}",
                variables = "L = \\text{distributed inductance (H/m)}, C = \\text{capacitance (F/m)}",
                hash = TextImportParser.sha256("Z₀ = √(L / C)")
            ),
            FormulaEntity(
                id = 5,
                subjectId = 2,
                questionId = 2,
                name = "Voltage Reflection Coefficient",
                formula = "\\Gamma = \\frac{Z_L - Z_0}{Z_L + Z_0}",
                variables = "Z_L = \\text{load impedance}, Z_0 = \\text{line impedance}",
                hash = TextImportParser.sha256("Γ = (Z_L - Z₀) / (Z_L + Z₀)")
            ),
            FormulaEntity(
                id = 6,
                subjectId = 2,
                questionId = 2,
                name = "Voltage Standing Wave Ratio",
                formula = "\\text{VSWR} = \\frac{1 + |\\Gamma|}{1 - |\\Gamma|}",
                variables = "|\\Gamma| = \\text{magnitude of reflection coefficient}",
                hash = TextImportParser.sha256("VSWR = (1 + |Γ|) / (1 - |Γ|)")
            ),
            FormulaEntity(
                id = 7,
                subjectId = 3,
                questionId = 3,
                name = "Parallel Complex Impedances",
                formula = "Z_{eq} = \\frac{Z_1 \\cdot Z_2}{Z_1 + Z_2}",
                variables = "Z_1, Z_2 = \\text{complex branch impedances}",
                hash = TextImportParser.sha256("Z_eq = (Z₁ · Z₂) / (Z₁ + Z₂)")
            ),
            FormulaEntity(
                id = 8,
                subjectId = 4,
                questionId = 4,
                name = "Sinking Fund Depreciation",
                formula = "d = (C - C_L) \\cdot \\frac{i}{(1 + i)^n - 1}",
                variables = "C = \\text{first cost}, C_L = \\text{salvage value}, i = \\text{interest}, n = \\text{years}",
                hash = TextImportParser.sha256("d = (C - C_L) · [i / ((1 + i)ⁿ - 1)]")
            )
        )
    }

    fun getInitialTechniques(): List<CalcuTechniqueEntity> {
        return listOf(
            CalcuTechniqueEntity(
                id = 1,
                subjectId = 1,
                questionId = 1,
                title = "Fast BJT DC Bias Evaluation",
                keystrokes = "Mode: COMP [MODE] [1]\n1. Enter 150 × (25 × 10^{-6}) and press [=]\n2. Press [ENG] → 3.75 × 10^{-3} A\n3. Enter 15 - (3.75 × 10^{-3} × 2400) [=] → 6.0 V",
                calculatorModel = "Canon F-789SGA",
                notes = "",
                hash = TextImportParser.sha256("bjt_dc_bias_mult_v2")
            ),
            CalcuTechniqueEntity(
                id = 2,
                subjectId = 2,
                questionId = 2,
                title = "Complex Reflection Coefficient in Polar Form",
                keystrokes = "Mode: CMPLX [MODE] [2]\n1. Enter (25 + 50i) ÷ (125 + 50i) using [ENG] for i\n2. Press [APPS] [1:▶r∠θ] [=] → 0.415∠41.63°\n3. Enter (1 + 0.415) ÷ (1 - 0.415) [=] → 2.42",
                calculatorModel = "Canon F-789SGA",
                notes = "",
                hash = TextImportParser.sha256("complex_division_polar_v2")
            ),
            CalcuTechniqueEntity(
                id = 3,
                subjectId = 3,
                questionId = 3,
                title = "Parallel Complex Impedances & Limit [CALC]",
                keystrokes = "Mode: CMPLX [MODE] [2]\n1. Enter 1 ÷ (1 ÷ (12 + 16i) + 1 ÷ (8 - 6i)) and press [APPS] [1:▶r∠θ] [=]\n2. Rectangular output → 7.2 + j1.6 Ω\n3. Polar output → 7.37∠12.53° Ω",
                calculatorModel = "Canon F-789SGA",
                notes = "",
                hash = TextImportParser.sha256("parallel_complex_reciprocal_v2")
            ),
            CalcuTechniqueEntity(
                id = 4,
                subjectId = 4,
                questionId = 4,
                title = "Compound Interest & Sinking Fund Power",
                keystrokes = "Mode: COMP [MODE] [1]\n1. Enter 450000 × (0.08 ÷ ((1 + X)^{10} - 1)) and press [CALC]\n2. X = 0.08 → 31,063.27\n3. Book Value BV_5 = 500000 - 182236.80 → 317,763.20",
                calculatorModel = "Canon F-789SGA",
                notes = "",
                hash = TextImportParser.sha256("sinking_fund_canon_v2")
            ),
            CalcuTechniqueEntity(
                id = 5,
                subjectId = 3,
                questionId = null,
                title = "Rational Limit Evaluation via [CALC]",
                keystrokes = "Mode: COMP [MODE] [1]\n1. Enter (3X^2 + 1) ÷ (X^2 - 5) and press [CALC]\n2. X = 100 → 3.001601\n3. X = 10000 → 3.000002",
                calculatorModel = "Canon F-789SGA",
                notes = "",
                hash = TextImportParser.sha256("limit_calc_canon_v2")
            )
        )
    }

    val SAMPLE_IMPORT_TEXT = """
[Q] An FM signal has frequency deviation [IEQ]\Delta f = 75\text{ kHz}[/IEQ] and modulating frequency [IEQ]f_m = 15\text{ kHz}[/IEQ]. Calculate the modulation index [IEQ]\beta[/IEQ] and determine the approximate bandwidth using Carson's Rule.
[A] [IEQ]\beta = 5.0[/IEQ], [IEQ]\text{BW} = 180\text{ kHz}[/IEQ]
[SOL]
Step 1: Compute FM Modulation Index [IEQ]\beta[/IEQ]:
[EQ]\beta = \frac{\Delta f}{f_m} = \frac{75\text{ kHz}}{15\text{ kHz}} = 5.0[/EQ]

Step 2: Apply Carson's Rule for Bandwidth:
[EQ]\text{BW} = 2 \cdot (\Delta f + f_m) = 2 \cdot (75 + 15) = 180\text{ kHz}[/EQ]
[FORMULA] FM Modulation Index: [EQ]\beta = \frac{\Delta f}{f_m}[/EQ] | \Delta f = \text{deviation}, f_m = \text{modulating frequency}
[FORMULA] Carson's Rule: [EQ]\text{BW} = 2 \cdot f_m \cdot (1 + \beta)[/EQ] | \text{BW} = \text{minimum transmission bandwidth}
[CALCU] Quick Carson's Rule via [CALC]
Mode: COMP [MODE] [1]
1. Enter 2 × (A + B) and press [CALC]
2. A = 75, B = 15 [=] → 180 kHz
3. Modulation Index 75 ÷ 15 [=] → 5.0

[Q] A series RLC resonant circuit has resistance [IEQ]R = 10\text{ }\Omega[/IEQ], inductance [IEQ]L = 100\text{ }\mu\text{H}[/IEQ], and capacitance [IEQ]C = 10\text{ pF}[/IEQ]. Determine the resonant frequency [IEQ]f_0[/IEQ] in MHz and the circuit Quality Factor [IEQ]Q[/IEQ].
[A] [IEQ]f_0 = 5.033\text{ MHz}[/IEQ], [IEQ]Q = 316.23[/IEQ]
[SOL]
Step 1: Calculate Resonant Frequency [IEQ]f_0[/IEQ]:
[EQ]f_0 = \frac{1}{2\pi \sqrt{L \cdot C}}[/EQ]
[EQ]f_0 = \frac{1}{2\pi \sqrt{(100 \times 10^{-6})(10 \times 10^{-12})}} = 5.033\text{ MHz}[/EQ]

Step 2: Calculate Quality Factor [IEQ]Q[/IEQ]:
[EQ]Q = \frac{1}{R}\sqrt{\frac{L}{C}} = \frac{1}{10}\sqrt{\frac{100 \times 10^{-6}}{10 \times 10^{-12}}} = 316.23[/EQ]
[FORMULA] Series RLC Resonance: [EQ]f_0 = \frac{1}{2\pi\sqrt{LC}}[/EQ] | L \text{ in Henries}, C \text{ in Farads}
[FORMULA] Quality Factor: [EQ]Q = \frac{1}{R}\sqrt{\frac{L}{C}}[/EQ] | \text{Dimensionless figure of merit}
[CALCU] Series RLC Resonance & Q-Factor
Mode: COMP [MODE] [1]
1. Enter 1 ÷ (2π × √(100 × 10^{-6} × 10 × 10^{-12})) and press [=]
2. Press [ENG] → 5.033 × 10^6 Hz
3. Enter (1 ÷ 10) × √(100 × 10^{-6} ÷ (10 × 10^{-12})) [=] → 316.23

[Q] What condition must be satisfied by the loop gain for sustained oscillations to occur according to the Barkhausen criterion?
[A] The magnitude of the loop gain must equal unity ([IEQ]|A\beta| = 1[/IEQ]) and the total phase shift around the feedback loop must be [IEQ]0^\circ[/IEQ] (or an integer multiple of [IEQ]360^\circ[/IEQ]).
""".trimIndent()
}
