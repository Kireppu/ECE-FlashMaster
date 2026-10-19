package com.example.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * Utility for formatting mathematical and engineering expressions into pristine
 * Microsoft Word / Google Docs-style Unicode Math & AnnotatedString typography
 * without any LaTeX or WebViews. Supports native Cambria Math font styling.
 */
object UnicodeMathUtils {

    private val superscriptMap = mapOf(
        '0' to '⁰', '1' to '¹', '2' to '²', '3' to '³', '4' to '⁴',
        '5' to '⁵', '6' to '⁶', '7' to '⁷', '8' to '⁸', '9' to '⁹',
        '+' to '⁺', '-' to '⁻', '=' to '⁼', '(' to '⁽', ')' to '⁾',
        'n' to 'ⁿ', 'i' to 'ⁱ', 'x' to 'ˣ', 'y' to 'ʸ', 't' to 'ᵗ',
        'a' to 'ᵃ', 'b' to 'ᵇ', 'c' to 'ᶜ', 'd' to 'ᵈ', 'e' to 'ᵉ',
        'k' to 'ᵏ', 'm' to 'ᵐ', 'r' to 'ʳ', 's' to 'ˢ'
    )

    private val subscriptMap = mapOf(
        '0' to "₀", '1' to "₁", '2' to "₂", '3' to "₃", '4' to "₄",
        '5' to "₅", '6' to "₆", '7' to "₇", '8' to "₈", '9' to "₉",
        '+' to "₊", '-' to "₋", '=' to "₌", '(' to "₍", ')' to "₎",
        'a' to "ₐ", 'e' to "ₑ", 'h' to "ₕ", 'i' to "ᵢ", 'j' to "ⱼ",
        'k' to "ₖ", 'l' to "ₗ", 'm' to "ₘ", 'n' to "ₙ", 'o' to "ₒ",
        'p' to "ₚ", 'r' to "ᵣ", 's' to "ₛ", 't' to "ₜ", 'u' to "ᵤ",
        'v' to "ᵥ", 'x' to "ₓ"
    )

    // Case-sensitive Greek & Symbol replacements so \Omega -> Ω, \omega -> ω,
    // and \muA, \muF, \muV, \muH, \mu\Omega, \mu\text{A} -> μA, μF, μV, etc.
    private val symbolReplacements = listOf(
        // Uppercase Greek Letters (MUST come before lowercase patterns)
        Regex("""\\Omega(?![a-zA-Z])""") to "Ω",
        Regex("""\\Gamma(?![a-zA-Z])""") to "Γ",
        Regex("""\\Delta(?![a-zA-Z])|\bDelta\b""") to "Δ",
        Regex("""\\Theta(?![a-zA-Z])""") to "Θ",
        Regex("""\\Lambda(?![a-zA-Z])""") to "Λ",
        Regex("""\\Pi(?![a-zA-Z])""") to "Π",
        Regex("""\\Sigma(?![a-zA-Z])""") to "Σ",
        Regex("""\\Phi(?![a-zA-Z])""") to "Φ",
        Regex("""\\Psi(?![a-zA-Z])""") to "Ψ",

        // Micro prefix before any unit (e.g. \muA, \muF, \muH, \muV, \mus, \mum, \muW, \muC, \mu\Omega),
        // subscripts/superscripts, delimiters, or standalone \mu / \bmicro\b
        Regex("""\\mu(?:(?=[AFHVsmWCJΩg]|_|\^|\{|\s|[0-9\],.;/+\-=×·÷*\\∠°()]|$))|\bmicro\b""") to "μ",
        Regex("""\\mu(?=[a-zA-Z](?![a-zA-Z]))""") to "μ",

        // Lowercase Greek Letters
        Regex("""\\alpha(?![a-zA-Z])|\balpha\b""") to "α",
        Regex("""\\beta(?![a-zA-Z])|\bbeta\b""") to "β",
        Regex("""\\gamma(?![a-zA-Z])|\bgamma\b""") to "γ",
        Regex("""\\delta(?![a-zA-Z])|\bdelta\b""") to "δ",
        Regex("""\\epsilon(?![a-zA-Z])|\\varepsilon(?![a-zA-Z])|\bepsilon\b""") to "ε",
        Regex("""\\zeta(?![a-zA-Z])|\bzeta\b""") to "ζ",
        Regex("""\\eta(?![a-zA-Z])""") to "η",
        Regex("""\\theta(?![a-zA-Z])|\btheta\b""") to "θ",
        Regex("""\\lambda(?![a-zA-Z])|\blambda\b""") to "λ",
        Regex("""\\pi(?![a-zA-Z])|\bpi\b""") to "π",
        Regex("""\\rho(?![a-zA-Z])|\brho\b""") to "ρ",
        Regex("""\\sigma(?![a-zA-Z])|\bsigma\b""") to "σ",
        Regex("""\\tau(?![a-zA-Z])|\btau\b""") to "τ",
        Regex("""\\phi(?![a-zA-Z])|\bphi\b""") to "φ",
        Regex("""\\omega(?![a-zA-Z])|\bomega\b""") to "ω",

        // Operators & Calculus
        Regex("""\\sum(?![a-zA-Z])""") to "∑",
        Regex("""\\prod(?![a-zA-Z])""") to "∏",
        Regex("""\\int(?![a-zA-Z])""") to "∫",
        Regex("""\\iint(?![a-zA-Z])""") to "∬",
        Regex("""\\iiint(?![a-zA-Z])""") to "∭",
        Regex("""\\oint(?![a-zA-Z])""") to "∮",
        Regex("""\\partial(?![a-zA-Z])""") to "∂",
        Regex("""\\nabla(?![a-zA-Z])""") to "∇",

        // Engineering circuit operators & relations
        Regex("""\\parallel(?![a-zA-Z])|\|\|""") to "∥",
        Regex("""\\perp(?![a-zA-Z])""") to "⊥",
        Regex("""\\therefore(?![a-zA-Z])""") to "∴",
        Regex("""\\because(?![a-zA-Z])""") to "∵",
        Regex("""\\iff(?![a-zA-Z])|\\(?:long)?Leftrightarrow(?![a-zA-Z])|<=>""") to "⇔",
        Regex("""\\implies(?![a-zA-Z])|\\(?:long)?Rightarrow(?![a-zA-Z])|=>""") to "⇒",
        Regex("""\\(?:long)?Leftarrow(?![a-zA-Z])|<=""") to "⇐",
        Regex("""\\(?:long)?leftrightarrow(?![a-zA-Z])|<->""") to "↔",
        Regex("""\\(?:long)?rightarrow(?![a-zA-Z])|\\to(?![a-zA-Z])|->""") to "→",
        Regex("""\\(?:long)?leftarrow(?![a-zA-Z])|\\gets(?![a-zA-Z])|<-""") to "←",
        Regex("""\\uparrow(?![a-zA-Z])""") to "↑",
        Regex("""\\downarrow(?![a-zA-Z])""") to "↓",
        Regex("""\\forall(?![a-zA-Z])""") to "∀",
        Regex("""\\exists(?![a-zA-Z])""") to "∃",
        Regex("""\\in(?![a-zA-Z])""") to "∈",
        Regex("""\\notin(?![a-zA-Z])""") to "∉",
        Regex("""\\subset(?![a-zA-Z])""") to "⊂",
        Regex("""\\supset(?![a-zA-Z])""") to "⊃",
        Regex("""\\cup(?![a-zA-Z])""") to "∪",
        Regex("""\\cap(?![a-zA-Z])""") to "∩",

        // Engineering units and symbols
        Regex("(?i)\\bohms?\\b") to "Ω",
        Regex("(?i)\\\\(?:angle|measuredangle)(?![a-zA-Z])|\\bangle\\b") to "∠",
        Regex("(?i)\\\\pm(?![a-zA-Z])|\\+\\/-") to "±",
        Regex("(?i)\\\\mp(?![a-zA-Z])|-\\/\\+") to "∓",
        Regex("(?i)\\\\times(?![a-zA-Z])") to "×",
        Regex("(?i)\\\\div(?![a-zA-Z])") to "÷",
        Regex("(?i)\\\\cdot(?![a-zA-Z])|\\\\bullet(?![a-zA-Z])") to "·",
        Regex("(?i)\\\\approx(?![a-zA-Z])") to "≈",
        Regex("(?i)\\\\equiv(?![a-zA-Z])") to "≡",
        Regex("(?i)\\\\propto(?![a-zA-Z])") to "∝",
        Regex("(?i)\\\\(?:neq|ne)(?![a-zA-Z])|!=") to "≠",
        Regex("(?i)\\\\(?:leq|le)(?![a-zA-Z])|<=") to "≤",
        Regex("(?i)\\\\(?:geq|ge)(?![a-zA-Z])|>=") to "≥",
        Regex("(?i)\\\\ll(?![a-zA-Z])") to "≪",
        Regex("(?i)\\\\gg(?![a-zA-Z])") to "≫",
        Regex("(?i)\\\\infty(?![a-zA-Z])|\\binfinity\\b") to "∞",
        Regex("""\^?\{?\\circ\}?|(?i)\\(?:degree|deg)\b""") to "°",
        Regex("""\\(?:ldots|dots|cdots)(?![a-zA-Z])""") to "…",
        Regex("\\\\%") to "%"
    )

    /**
     * Extracts a balanced `{...}` group starting at [openBraceIndex].
     * Returns Pair(innerString, nextIndexAfterCloseBrace) or null if unbalanced.
     */
    fun extractBracedGroup(input: String, openBraceIndex: Int): Pair<String, Int>? {
        if (openBraceIndex >= input.length || input[openBraceIndex] != '{') return null
        var depth = 0
        var i = openBraceIndex
        while (i < input.length) {
            when (input[i]) {
                '{' -> depth++
                '}' -> {
                    depth--
                    if (depth == 0) {
                        return Pair(input.substring(openBraceIndex + 1, i), i + 1)
                    }
                }
            }
            i++
        }
        return Pair(input.substring(openBraceIndex + 1), input.length)
    }

    /**
     * Replaces LaTeX fractions (\frac, \dfrac, \tfrac, \cfrac) with clean (num / den)
     * using robust balanced-brace extraction that preserves nested subscripts and square roots.
     */
    fun replaceFractions(input: String): String {
        val fracCommands = listOf("\\frac", "\\dfrac", "\\tfrac", "\\cfrac")
        var text = input
        var changed: Boolean
        do {
            changed = false
            for (cmd in fracCommands) {
                val idx = text.indexOf(cmd)
                if (idx >= 0) {
                    var cursor = idx + cmd.length
                    while (cursor < text.length && text[cursor].isWhitespace()) cursor++
                    val numGroup = extractBracedGroup(text, cursor)
                    if (numGroup != null) {
                        cursor = numGroup.second
                        while (cursor < text.length && text[cursor].isWhitespace()) cursor++
                        val denGroup = extractBracedGroup(text, cursor)
                        if (denGroup != null) {
                            val num = replaceFractions(numGroup.first)
                            val den = replaceFractions(denGroup.first)
                            text = text.substring(0, idx) + "($num / $den)" + text.substring(denGroup.second)
                            changed = true
                            break
                        }
                    }
                }
            }
        } while (changed)
        return text
    }

    /**
     * Replaces LaTeX square roots (\sqrt{...}) with clean √(inner)
     * using robust balanced-brace extraction that never cuts off at inner braces.
     */
    fun replaceRadicals(input: String): String {
        var text = input
        var changed: Boolean
        do {
            changed = false
            val idx = text.indexOf("\\sqrt")
            if (idx >= 0) {
                var cursor = idx + 5
                while (cursor < text.length && text[cursor].isWhitespace()) cursor++
                val radGroup = extractBracedGroup(text, cursor)
                if (radGroup != null) {
                    val inner = replaceRadicals(radGroup.first)
                    text = text.substring(0, idx) + "√($inner)" + text.substring(radGroup.second)
                    changed = true
                } else {
                    text = text.substring(0, idx) + "√" + text.substring(idx + 5)
                    changed = true
                }
            }
        } while (changed)
        return text
    }

    /**
     * Strips LaTeX text/formatting wrappers like \text{...}, \mathrm{...}, \left, \right
     * while preserving inner text and converting Greek/math symbols accurately.
     */
    fun stripLatexWrappers(input: String): String {
        var text = input
        // Strip custom equation tags
        text = text.replace("[EQ]", "").replace("[/EQ]", "")
            .replace("[IEQ]", "").replace("[/IEQ]", "")

        // Strip LaTeX display math delimiters: \[...\] and $$...$$
        text = text.replace(Regex("""\\\[([\s\S]*?)\\\]"""), "$1")
        text = text.replace(Regex("""\$\$([\s\S]*?)\$\$"""), "$1")

        // Strip LaTeX inline math delimiters: \(...\) and $...$
        text = text.replace(Regex("""\\\(([\s\S]*?)\\\)"""), "$1")
        text = text.replace(Regex("""\$([^$]+)\$"""), "$1")

        // Strip LaTeX environments: equation, align, gather, cases, matrix, pmatrix, array, etc.
        text = text.replace(Regex("""\\begin\{(?:equation|align|gather|cases|matrix|pmatrix|bmatrix|array|split|aligned)\*?\}"""), "")
        text = text.replace(Regex("""\\end\{(?:equation|align|gather|cases|matrix|pmatrix|bmatrix|array|split|aligned)\*?\}"""), "")

        // Clean LaTeX linebreaks \\ and column separators &
        text = text.replace(Regex("""\\\\(?:\s*\[[\d\w.]+\])?"""), "\n")
        text = text.replace("&", " ")

        // Strip backslash from standard mathematical/trig functions
        text = text.replace(Regex("""\\(sin|cos|tan|sec|csc|cot|arcsin|arccos|arctan|ln|log|exp|det|min|max|lim|sinh|cosh|tanh)(?![a-zA-Z])"""), "$1")

        // 1. Apply Greek and symbol replacements FIRST before stripping \text{...}
        //    so sequences like \mu\text{A} or \muA convert \mu -> μ accurately!
        for ((regex, replacement) in symbolReplacements) {
            text = text.replace(regex, replacement)
        }

        // 2. Replace \text{...}, \mathrm{...}, \mathbf{...}, \mathit{...}, \bm{...}
        var hadTextWrapper: Boolean
        do {
            val prev = text
            text = text.replace(Regex("""\\(?:text|mathrm|mathbf|mathit|textbf|textit|bm)\{([^}]*)\}"""), "$1")
            hadTextWrapper = text != prev
        } while (hadTextWrapper)

        // 3. Re-apply symbol replacements in case any symbols were inside \text{...}
        for ((regex, replacement) in symbolReplacements) {
            text = text.replace(regex, replacement)
        }

        // Replace accents like \check{c}, \hat{x}, \bar{x}, \vec{v}
        text = text.replace(Regex("""\\check\{([^}]+)\}"""), "$1̌")
        text = text.replace(Regex("""\\hat\{([^}]+)\}"""), "$1̂")
        text = text.replace(Regex("""\\bar\{([^}]+)\}"""), "$1̄")
        text = text.replace(Regex("""\\vec\{([^}]+)\}"""), "$1⃗")

        // Strip \left and \right sizing commands without damaging \rightarrow or \leftarrow!
        text = text.replace(Regex("""\\left\s*([(\[.|{\\])"""), "$1")
        text = text.replace(Regex("""\\right\s*([)\].|}\\])"""), "$1")
        text = text.replace(Regex("""\\left\b(?![a-zA-Z])"""), "")
        text = text.replace(Regex("""\\right\b(?![a-zA-Z])"""), "")
        text = text.replace(Regex("""\\(?:big|Big|bigg|Bigg)[lrm]?\s*"""), "")

        // Replace LaTeX spacing commands
        text = text.replace("\\,", " ")
            .replace("\\;", " ")
            .replace("\\!", "")
            .replace("\\quad", "  ")
            .replace("\\qquad", "    ")

        return text
    }

    /**
     * Converts raw text, LaTeX fragments, or ASCII equations into clean
     * Microsoft Word / Docs style plain String Unicode math.
     */
    fun formatToUnicodeMath(rawText: String): String {
        var text = stripLatexWrappers(rawText)
        text = replaceRadicals(text)
        text = replaceFractions(text)

        // Handle bracketed superscripts: ^{...}
        text = text.replace(Regex("""\^\{([^}]+)\}""")) { match ->
            val content = match.groupValues[1]
            content.map { superscriptMap[it] ?: it }.joinToString("")
        }

        // Handle simple numeric/variable superscripts: ^-?[0-9]+
        text = text.replace(Regex("""\^([\-+]?[0-9nixytab]+)""")) { match ->
            val content = match.groupValues[1]
            content.map { superscriptMap[it] ?: it }.joinToString("")
        }

        // Handle bracketed subscripts: _{...}
        text = text.replace(Regex("""_\{([^}]+)\}""")) { match ->
            val content = match.groupValues[1]
            convertSubscriptString(content)
        }

        // Handle simple numeric subscripts: _[0-9]+
        text = text.replace(Regex("""_([0-9]+)""")) { match ->
            val content = match.groupValues[1]
            convertSubscriptString(content)
        }

        return text
    }

    /**
     * Builds a rich Compose AnnotatedString that renders true subscripts (e.g., Z_L, V_{CE}, I_C)
     * and true superscripts (e.g., 10^{-6}, X^2) with proper BaselineShift and font scaling,
     * preserving uppercase/lowercase engineering notation accurately.
     */
    fun formatToAnnotatedMath(
        rawText: String,
        baseFontSize: TextUnit = 16.sp,
        mathFontFamily: FontFamily? = null
    ): AnnotatedString {
        var text = stripLatexWrappers(rawText)
        text = replaceRadicals(text)
        text = replaceFractions(text)

        val subFontSize = (baseFontSize.value * 0.72f).sp

        // Tokenize _{...}, _[a-zA-Z0-9]+, ^{...}, ^[\-+]?[a-zA-Z0-9]+
        val scriptRegex = Regex("""(_\{[^}]+\}|_[a-zA-Z0-9]+|\^\{[^}]+\}|\^[\-+]?[a-zA-Z0-9]+)""")

        return buildAnnotatedString {
            var lastIndex = 0
            for (match in scriptRegex.findAll(text)) {
                if (match.range.first > lastIndex) {
                    append(text.substring(lastIndex, match.range.first))
                }
                val token = match.value
                val isSub = token.startsWith("_")
                val rawContent = if (token.length > 2 && token[1] == '{' && token.endsWith("}")) {
                    token.substring(2, token.length - 1)
                } else {
                    token.substring(1)
                }

                pushStyle(
                    SpanStyle(
                        baselineShift = if (isSub) BaselineShift(-0.22f) else BaselineShift(0.32f),
                        fontSize = subFontSize,
                        fontWeight = FontWeight.Medium,
                        fontFamily = mathFontFamily
                    )
                )
                append(rawContent)
                pop()

                lastIndex = match.range.last + 1
            }
            if (lastIndex < text.length) {
                append(text.substring(lastIndex))
            }
        }
    }

    private fun convertSubscriptString(str: String): String {
        val allMappable = str.all { subscriptMap.containsKey(it) }
        return if (allMappable) {
            str.map { ch -> subscriptMap[ch] ?: ch.toString() }.joinToString("")
        } else {
            str
        }
    }

    /**
     * Parses calculator keystrokes in format like: [MODE] [2:CMPLX] [SHIFT] [2] [3]
     * Returns a list of individual button labels.
     */
    fun parseKeycaps(keystrokes: String): List<String> {
        val matches = Regex("""\[([^\]]+)\]""").findAll(keystrokes)
        val list = matches.map { it.groupValues[1].trim() }.filter { it.isNotEmpty() }.toList()
        if (list.isNotEmpty()) return list

        return keystrokes
            .split(Regex("""\s*(->|,|\+)\s*|\s+"""))
            .map { it.trim().removeSurrounding("[", "]") }
            .filter { it.isNotEmpty() }
    }
}
