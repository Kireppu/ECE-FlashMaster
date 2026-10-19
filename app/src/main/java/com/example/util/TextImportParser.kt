package com.example.util

import java.security.MessageDigest

/**
 * Data models produced by the deterministic delimiter parsing engine.
 */
data class ParsedFormulaItem(
    val name: String,
    val formula: String,
    val variables: String = ""
)

data class ParsedCalcuItem(
    val title: String,
    val keystrokes: String,
    val notes: String = "",
    val model: String = "Canon F-789SGA"
)

data class ParsedFlashcard(
    val stem: String,
    val answer: String,
    val solution: String = "",
    val formulas: List<ParsedFormulaItem> = emptyList(),
    val calcuTechniques: List<ParsedCalcuItem> = emptyList(),
    val stemHash: String = ""
)

data class ParseResult(
    val items: List<ParsedFlashcard>,
    val totalFound: Int,
    val warnings: List<String> = emptyList(),
    val rawBlockCount: Int = 0,
    val duplicateCount: Int = 0
)

/**
 * Deterministic Plain-Text Ingestion Engine for ECE FlashMaster.
 * Pure Kotlin parser that splits and extracts flashcard items without any cloud AI or network dependencies,
 * featuring strict 1:1 question preservation, bulletproof sanitization, and SHA-256 deduplication.
 */
object TextImportParser {

    private val QUESTION_DELIMITER_REGEX = Regex(
        "(?im)(?:^|\\n)\\s*(?:\\d+[\\.\\)]\\s*)?(?:\\[Q\\]|Question\\s*\\d*\\s*:|QUESTION\\s*\\d*\\s*:|^Q\\s*\\d*\\s*:)\\s*"
    )

    private val ANSWER_DELIMITER_REGEX = Regex(
        "(?im)(?:^|\\n)\\s*(?:\\[A\\]|Answer\\s*:|ANSWER\\s*:|^A\\s*:|Ans\\s*:|ANS\\s*:)\\s*"
    )

    private val SOLUTION_DELIMITER_REGEX = Regex(
        "(?im)(?:^|\\n)\\s*(?:\\[SOL\\]|Solution\\s*:|SOLUTION\\s*:|^SOL\\s*:|Steps\\s*:|Derivation\\s*:|Explanation\\s*:)\\s*"
    )

    private val FORMULA_DELIMITER_REGEX = Regex(
        "(?im)(?:^|\\n)\\s*(?:\\[FORMULA\\]|Formula\\s*:|FORMULA\\s*:|Formulas\\s*:)\\s*"
    )

    private val CALCU_DELIMITER_REGEX = Regex(
        "(?im)(?:^|\\n)\\s*(?:\\[CALCU\\]|Calcu\\s*Tech\\s*:|CALCU\\s*:|Calculator\\s*:|Keystrokes\\s*:|Shortcut\\s*:)\\s*"
    )

    fun parse(rawInput: String): ParseResult {
        val trimmed = rawInput.trim()
        if (trimmed.isEmpty()) {
            return ParseResult(emptyList(), 0, listOf("Input text is empty."))
        }

        val matches = QUESTION_DELIMITER_REGEX.findAll(trimmed).toList()
        if (matches.isEmpty()) {
            return ParseResult(
                emptyList(),
                0,
                listOf("No question markers ([Q] or Question:) found in the text.")
            )
        }

        val flashcards = mutableListOf<ParsedFlashcard>()
        val warnings = mutableListOf<String>()
        val seenHashes = mutableSetOf<String>()
        var duplicatesSkipped = 0

        for (i in matches.indices) {
            val startIndex = matches[i].range.last + 1
            val endIndex = if (i + 1 < matches.size) matches[i + 1].range.first else trimmed.length
            val block = trimmed.substring(startIndex, endIndex).trim()

            if (block.isEmpty()) continue

            val parsed = parseSingleBlock(block, i + 1, warnings)
            if (parsed != null) {
                if (seenHashes.contains(parsed.stemHash)) {
                    duplicatesSkipped++
                    val shortStem = if (parsed.stem.length > 55) parsed.stem.take(52) + "..." else parsed.stem
                    warnings.add("Duplicate question omitted (SHA-256 deduplicated): \"$shortStem\"")
                } else {
                    seenHashes.add(parsed.stemHash)
                    flashcards.add(parsed)
                }
            }
        }

        return ParseResult(
            items = flashcards,
            totalFound = flashcards.size,
            warnings = warnings,
            rawBlockCount = matches.size,
            duplicateCount = duplicatesSkipped
        )
    }

    private fun parseSingleBlock(
        block: String,
        itemIndex: Int,
        warnings: MutableList<String>
    ): ParsedFlashcard? {
        val answerMatch = ANSWER_DELIMITER_REGEX.find(block)
        val solMatch = SOLUTION_DELIMITER_REGEX.find(block)
        val formulaMatches = FORMULA_DELIMITER_REGEX.findAll(block).toList()
        val calcuMatches = CALCU_DELIMITER_REGEX.findAll(block).toList()

        val tagPositions = listOfNotNull(
            answerMatch?.range?.first,
            solMatch?.range?.first,
            formulaMatches.firstOrNull()?.range?.first,
            calcuMatches.firstOrNull()?.range?.first
        )

        val stemEnd = if (tagPositions.isNotEmpty()) tagPositions.minOrNull()!! else block.length
        val rawStem = block.substring(0, stemEnd).trim()

        if (rawStem.isEmpty()) {
            warnings.add("Item #$itemIndex has an empty question stem.")
            return null
        }

        val allTagRanges = buildList {
            if (answerMatch != null) add("A" to answerMatch.range)
            if (solMatch != null) add("SOL" to solMatch.range)
            formulaMatches.forEach { add("FORMULA" to it.range) }
            calcuMatches.forEach { add("CALCU" to it.range) }
        }.sortedBy { it.second.first }

        var rawAnswer = ""
        var rawSol = ""
        val formulas = mutableListOf<ParsedFormulaItem>()
        val calcuItems = mutableListOf<ParsedCalcuItem>()

        for (idx in allTagRanges.indices) {
            val (tagType, range) = allTagRanges[idx]
            val contentStart = range.last + 1
            val contentEnd = if (idx + 1 < allTagRanges.size) {
                allTagRanges[idx + 1].second.first
            } else {
                block.length
            }
            if (contentStart >= contentEnd) continue
            val sectionText = block.substring(contentStart, contentEnd).trim()
            if (sectionText.isEmpty()) continue

            when (tagType) {
                "A" -> rawAnswer = sectionText
                "SOL" -> rawSol = sectionText
                "FORMULA" -> formulas.addAll(parseFormulas(sectionText))
                "CALCU" -> calcuItems.addAll(parseCalcuTechniques(sectionText))
            }
        }

        val sanitizedStem = sanitizeMcqStem(rawStem)
        val formattedStem = sanitizedStem.trim()
        val formattedAnswer = rawAnswer.trim()
        val formattedSolution = rawSol.trim()
        val hash = computeStemHash(sanitizedStem)

        return ParsedFlashcard(
            stem = formattedStem,
            answer = formattedAnswer,
            solution = formattedSolution,
            formulas = formulas,
            calcuTechniques = calcuItems,
            stemHash = hash
        )
    }

    /**
     * Sanitizes question stem while preserving 1:1 question content:
     * - Strips leading item numbers (e.g., "1. ", "Question 4: ")
     * - Strips trailing multiple-choice options block (A., B., C., D.) ONLY when formatted as options,
     *   NEVER corrupting parenthetical expressions like "(in A)", "(in mA)", "(in V)", or "(in dB)".
     */
    fun sanitizeMcqStem(stem: String): String {
        var clean = stem.trim()

        // 1. Remove leading question numbering if user left it (e.g. "1. ", "12) ")
        clean = clean.replace(Regex("^\\s*\\d+[\\.\\)]\\s*"), "")

        // 2. Remove trailing multiple-choice options block if present on separate lines at the end:
        // e.g.:
        // A. 10 mA
        // B. 20 mA
        // C. 30 mA
        // D. 40 mA
        clean = clean.replace(Regex("(?im)(?:\\r?\\n\\s*[A-D][\\.\\)]\\s+[^\\n]*)+$"), "")

        // 3. Remove inline multiple-choice options ONLY when there is a true choice sequence
        // containing at least option A and option B (e.g. " A) 10 mA  B) 20 mA  C) 30 mA").
        // This avoids accidentally deleting parenthetical units like "(in mA)" or "(in A)".
        val inlineChoicesSequenceRegex = Regex(
            "(?i)(?:\\s{2,}|\\s*\\r?\\n)\\s*\\(?A[\\.\\)]\\s+.*?\\(?B[\\.\\)]\\s+.*$"
        )
        clean = clean.replace(inlineChoicesSequenceRegex, "")

        return clean.trim()
    }

    /**
     * Computes a resilient SHA-256 hash for deduplication:
     * Normalizes case, removes formatting tags ([EQ], [IEQ]) and non-alphanumeric noise,
     * ensuring identical questions with slight whitespace or tagging differences are accurately deduplicated.
     */
    fun computeStemHash(stem: String): String {
        val normalized = stem
            .replace(Regex("\\[/?I?EQ\\]"), "")
            .replace("$$", "")
            .replace("$", "")
            .replace("\\[", "")
            .replace("\\]", "")
            .replace("\\(", "")
            .replace("\\)", "")
            .lowercase()
            .replace(Regex("[^a-z0-9]"), "")

        return sha256(normalized.ifEmpty { stem.lowercase().trim() })
    }

    private fun parseFormulas(text: String): List<ParsedFormulaItem> {
        val list = mutableListOf<ParsedFormulaItem>()
        val lines = text.lines().map { it.trim() }.filter { it.isNotEmpty() }

        for (line in lines) {
            val cleanLine = line
                .replace("[EQ]", "")
                .replace("[/EQ]", "")
                .replace("$$", "")
                .replace("\\[", "")
                .replace("\\]", "")
                .trim()
            val parts = cleanLine.split("|").map { it.trim() }
            if (parts.size >= 2) {
                val firstPart = parts[0]
                if (firstPart.contains(":") && !firstPart.startsWith("http")) {
                    val colonParts = firstPart.split(":", limit = 2)
                    val name = colonParts[0].trim()
                    val f = colonParts[1].trim().removePrefix("$").removeSuffix("$").trim()
                    val vars = parts.drop(1).joinToString(" | ")
                    list.add(ParsedFormulaItem(name = name, formula = f, variables = vars))
                } else {
                    val f = parts[0].removePrefix("$").removeSuffix("$").trim()
                    val name = parts[1]
                    val vars = if (parts.size > 2) parts[2] else ""
                    list.add(ParsedFormulaItem(name = name, formula = f, variables = vars))
                }
            } else if (cleanLine.contains(":") && !cleanLine.startsWith("http")) {
                val colonParts = cleanLine.split(":", limit = 2)
                val name = colonParts[0].trim()
                val f = colonParts[1].trim().removePrefix("$").removeSuffix("$").trim()
                list.add(ParsedFormulaItem(name = name, formula = f))
            } else {
                val f = cleanLine.removePrefix("$").removeSuffix("$").trim()
                list.add(ParsedFormulaItem(name = "Key Formula", formula = f))
            }
        }
        return list
    }

    /**
     * Parses calculator shortcut techniques supporting BOTH:
     * 1. Multi-line structured guides (Title + Mode: ... + 1. ... + 2. ... + 3. ...)
     * 2. Single-line pipe/colon separated guides (`Title: [KEY1] [KEY2] | Notes`)
     */
    private fun parseCalcuTechniques(text: String): List<ParsedCalcuItem> {
        val lines = text.lines().map { it.trim() }.filter { it.isNotEmpty() }
        if (lines.isEmpty()) return emptyList()

        val hasMultiStepLines = lines.any {
            it.startsWith("Mode:", ignoreCase = true) || Regex("^\\d+\\.\\s+.*").matches(it)
        }

        if (hasMultiStepLines) {
            val firstLine = lines.first()
            val isFirstLineModeOrStep = firstLine.startsWith("Mode:", ignoreCase = true) ||
                Regex("^\\d+\\.\\s+.*").matches(firstLine)

            val title: String
            val bodyLines: List<String>

            if (!isFirstLineModeOrStep) {
                if (firstLine.contains(":") && !firstLine.startsWith("[")) {
                    val p = firstLine.split(":", limit = 2)
                    title = p[0].trim()
                    val remainder = p[1].trim()
                    bodyLines = if (remainder.isNotEmpty()) {
                        listOf(remainder) + lines.drop(1)
                    } else {
                        lines.drop(1)
                    }
                } else {
                    title = firstLine.trimEnd(':').trim()
                    bodyLines = lines.drop(1)
                }
            } else {
                title = "Canon F-789SGA Shortcut"
                bodyLines = lines
            }

            return listOf(
                ParsedCalcuItem(
                    title = title.ifEmpty { "Canon F-789SGA Shortcut" },
                    keystrokes = bodyLines.joinToString("\n"),
                    notes = ""
                )
            )
        }

        val list = mutableListOf<ParsedCalcuItem>()
        for (line in lines) {
            val parts = line.split("|").map { it.trim() }
            if (parts.size >= 2) {
                val firstPart = parts[0]
                if (firstPart.contains(":") && firstPart.contains("[")) {
                    val colonIdx = firstPart.indexOf(':')
                    val firstBracketIdx = firstPart.indexOf('[')
                    if (colonIdx in 1 until firstBracketIdx) {
                        val title = firstPart.substring(0, colonIdx).trim()
                        val keystrokes = firstPart.substring(colonIdx + 1).trim()
                        val notes = parts.drop(1).joinToString(" ")
                        list.add(ParsedCalcuItem(title = title, keystrokes = keystrokes, notes = notes))
                        continue
                    }
                }
                val title = parts[0]
                val keystrokes = parts[1]
                val notes = if (parts.size > 2) parts[2] else ""
                list.add(ParsedCalcuItem(title = title, keystrokes = keystrokes, notes = notes))
            } else if (line.contains(":")) {
                val colonIdx = line.indexOf(':')
                val firstBracketIdx = line.indexOf('[')
                if (firstBracketIdx == -1 || colonIdx < firstBracketIdx) {
                    val title = line.substring(0, colonIdx).trim()
                    val keystrokes = line.substring(colonIdx + 1).trim()
                    list.add(ParsedCalcuItem(title = title, keystrokes = keystrokes, notes = ""))
                } else {
                    list.add(ParsedCalcuItem(title = "Canon F-789SGA Shortcut", keystrokes = line, notes = ""))
                }
            } else {
                list.add(ParsedCalcuItem(title = "Canon F-789SGA Shortcut", keystrokes = line, notes = ""))
            }
        }
        return list
    }

    fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
