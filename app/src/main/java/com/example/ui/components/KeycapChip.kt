package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberSecondaryDark
import com.example.ui.theme.CambriaMathFontFamily
import com.example.util.UnicodeMathUtils

/**
 * Constant-size single-line badge for the calculator model (e.g. "Canon F-789SGA").
 * Never wraps or compresses even when the technique title is long.
 */
@Composable
fun CanonModelBadge(
    modelName: String = "Canon F-789SGA",
    modifier: Modifier = Modifier
) {
    val label = if (modelName.isBlank() || modelName.contains("Canon", ignoreCase = true)) {
        "Canon F-789SGA"
    } else {
        modelName
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(AmberSecondaryDark.copy(alpha = 0.14f))
            .border(1.dp, AmberSecondaryDark.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = AmberSecondaryDark,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false
        )
    }
}

/**
 * Renders a crisp inline calculator button keycap matching the reference UI
 * (e.g. [MODE], [1], [CALC], [SHIFT], [APPS]).
 */
@Composable
fun KeycapChip(
    keyLabel: String,
    modifier: Modifier = Modifier
) {
    val upper = keyLabel.uppercase()

    val (labelColor, borderColor, bgColor) = when {
        upper.contains("SHIFT") -> Triple(Color(0xFFFDBA74), Color(0x88FDBA74), Color(0xFF1C1917))
        upper.contains("ALPHA") -> Triple(Color(0xFFFCA5A5), Color(0x88FCA5A5), Color(0xFF1C1917))
        upper.contains("APPS") || upper.contains("CALC") || upper.contains("SOLVE") ->
            Triple(Color(0xFFFFFFFF), Color(0xFF71717A), Color(0xFF18181B))
        else -> Triple(Color(0xFFFFFFFF), Color(0xFF63636B), Color(0xFF18181B))
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = keyLabel,
            color = labelColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 0.3.sp,
            maxLines = 1,
            softWrap = false
        )
    }
}

/**
 * Data structure representing a parsed calculator technique with a Mode header
 * and numbered step-by-step instructions.
 */
private data class ParsedCalcuSteps(
    val modeName: String,
    val modeKeys: List<String>,
    val steps: List<String>
)

private fun parseCalcuTechnique(rawKeystrokes: String, rawNotes: String): ParsedCalcuSteps {
    val combined = if (rawNotes.isNotBlank() && !rawKeystrokes.contains(rawNotes)) {
        "$rawKeystrokes\n$rawNotes"
    } else {
        rawKeystrokes
    }
    val lines = combined.lines().map { it.trim() }.filter { it.isNotEmpty() }

    // 1. Determine Mode header
    var modeName = "COMP"
    var modeKeys = listOf("MODE", "1")

    val explicitModeLine = lines.firstOrNull { it.startsWith("Mode:", ignoreCase = true) }
    if (explicitModeLine != null) {
        val afterColon = explicitModeLine.substringAfter(":").trim()
        val extractedKeys = Regex("\\[([^\\]]+)\\]").findAll(afterColon).map { it.groupValues[1] }.toList()
        val namePart = afterColon.replace(Regex("\\[[^\\]]+\\]"), "").trim()
        if (namePart.isNotEmpty()) modeName = namePart
        if (extractedKeys.isNotEmpty()) modeKeys = extractedKeys
    } else if (combined.contains("CMPLX", ignoreCase = true) || combined.contains("2:CMPLX", ignoreCase = true)) {
        modeName = "CMPLX"
        modeKeys = listOf("MODE", "2")
    } else if (combined.contains("STAT", ignoreCase = true) || combined.contains("3:STAT", ignoreCase = true)) {
        modeName = "STAT"
        modeKeys = listOf("MODE", "3")
    } else if (combined.contains("EQN", ignoreCase = true) || combined.contains("5:EQN", ignoreCase = true)) {
        modeName = "EQN"
        modeKeys = listOf("MODE", "5")
    } else if (combined.contains("TABLE", ignoreCase = true) || combined.contains("7:TABLE", ignoreCase = true)) {
        modeName = "TABLE"
        modeKeys = listOf("MODE", "7")
    }

    // 2. Check if numbered steps (1., 2., 3.) already exist in lines
    val stepLines = lines.filter { !it.startsWith("Mode:", ignoreCase = true) }
    val numberedRegex = Regex("^\\d+\\.\\s*.*")
    if (stepLines.any { numberedRegex.matches(it) }) {
        val cleanedSteps = stepLines.map { line ->
            line.replace(Regex("^\\d+\\.\\s*"), "").trim()
        }.filter { it.isNotEmpty() }
        if (cleanedSteps.isNotEmpty()) {
            return ParsedCalcuSteps(modeName, modeKeys, cleanedSteps)
        }
    }

    // 3. Otherwise build clean numbered steps from keystrokes & notes
    val builtSteps = mutableListOf<String>()
    val cleanKeys = rawKeystrokes
        .replace(Regex("\\[MODE\\]\\s*\\[[0-9]:[^\\]]+\\]", RegexOption.IGNORE_CASE), "")
        .replace(Regex("Mode:\\s*[A-Za-z0-9\\s]+(?:\\[[^\\]]+\\]\\s*)*", RegexOption.IGNORE_CASE), "")
        .trim()

    if (cleanKeys.isNotEmpty()) {
        val prefix = if (cleanKeys.startsWith("Enter", ignoreCase = true) ||
            cleanKeys.startsWith("Press", ignoreCase = true)
        ) "" else "Enter "
        builtSteps.add("$prefix$cleanKeys")
    }

    if (rawNotes.isNotBlank()) {
        val noteParts = rawNotes
            .split(Regex("(?<=\\.)\\s+|\\n+"))
            .map { it.trim().trimEnd('.') }
            .filter { it.isNotEmpty() }
        for (part in noteParts) {
            val formattedPart = if (part.contains("→") || part.contains("->")) {
                part
            } else {
                part.replace(
                    Regex("^(?:Directly yields|Directly gives|Gives|Displays|Result is|Yields)\\s+", RegexOption.IGNORE_CASE),
                    "Result → "
                )
            }
            builtSteps.add(formattedPart)
        }
    }

    if (builtSteps.isEmpty() && rawKeystrokes.isNotBlank()) {
        builtSteps.add(rawKeystrokes.trim())
    }

    return ParsedCalcuSteps(modeName, modeKeys, builtSteps)
}

/**
 * Renders a calculator technique in the structured reference layout:
 * - Top line: `Mode: COMP [MODE] [1]` (wrapping gracefully via FlowRow on small screens)
 * - Dark step box with a solid vertical green accent bar drawn via drawBehind
 *   (so multi-line FlowRow steps never get clipped on narrow phone screens)
 * - Numbered steps (`1.`, `2.`, `3.`) with inline `[KEY]` keycaps and green results after `→`.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CalcuTechniqueBlock(
    rawKeystrokes: String,
    notes: String = "",
    modifier: Modifier = Modifier
) {
    val parsed = remember(rawKeystrokes, notes) {
        parseCalcuTechnique(rawKeystrokes, notes)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Top Mode Header: "Mode: COMP [MODE] [1]"
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Mode:",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            Text(
                text = parsed.modeName,
                color = Color(0xFFE4E4E7),
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            parsed.modeKeys.forEach { key ->
                KeycapChip(
                    keyLabel = key,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
        }

        // Step Container with solid left emerald green accent bar drawn dynamically
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF141519))
                .border(1.dp, Color(0xFF27272F), RoundedCornerShape(8.dp))
                .drawBehind {
                    val barWidth = 4.dp.toPx()
                    drawRect(
                        color = Color(0xFF22C55E),
                        topLeft = Offset.Zero,
                        size = Size(barWidth, size.height)
                    )
                }
                .padding(start = 14.dp, end = 10.dp, top = 10.dp, bottom = 10.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                parsed.steps.forEachIndexed { index, stepText ->
                    CalcuStepRow(stepNumber = index + 1, stepText = stepText)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CalcuStepRow(
    stepNumber: Int,
    stepText: String
) {
    // Split by arrow "→" or "->" so the output result after the arrow is highlighted in green
    val normalized = stepText.replace("->", "→")
    val hasArrow = normalized.contains("→")
    val beforeArrow = if (hasArrow) normalized.substringBefore("→").trim() else normalized
    val afterArrow = if (hasArrow) normalized.substringAfter("→").trim() else ""

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "$stepNumber.",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            modifier = Modifier
                .widthIn(min = 20.dp)
                .padding(top = 1.dp)
        )

        FlowRow(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val tokenRegex = Regex("\\[([^\\]]+)\\]")

            // Render tokens before arrow
            var lastIdx = 0
            for (match in tokenRegex.findAll(beforeArrow)) {
                if (match.range.first > lastIdx) {
                    val seg = beforeArrow.substring(lastIdx, match.range.first).trim()
                    if (seg.isNotEmpty()) {
                        val words = seg.split(Regex("\\s+")).filter { it.isNotEmpty() }
                        words.forEach { word ->
                            val annotated = remember(word) {
                                UnicodeMathUtils.formatToAnnotatedMath(word, 13.sp)
                            }
                            Text(
                                text = annotated,
                                color = Color(0xFFE4E4E7),
                                fontSize = 13.sp,
                                modifier = Modifier.align(Alignment.CenterVertically)
                            )
                        }
                    }
                }
                KeycapChip(
                    keyLabel = match.groupValues[1],
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                lastIdx = match.range.last + 1
            }
            if (lastIdx < beforeArrow.length) {
                val tail = beforeArrow.substring(lastIdx).trim()
                if (tail.isNotEmpty()) {
                    val words = tail.split(Regex("\\s+")).filter { it.isNotEmpty() }
                    words.forEach { word ->
                        val annotated = remember(word) {
                            UnicodeMathUtils.formatToAnnotatedMath(word, 13.sp)
                        }
                        Text(
                            text = annotated,
                            color = Color(0xFFE4E4E7),
                            fontSize = 13.sp,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                    }
                }
            }

            // Render arrow and green result tokens (word-wrapped so narrow screens never clip)
            if (hasArrow && afterArrow.isNotEmpty()) {
                Text(
                    text = "→",
                    color = Color(0xFFA1A1AA),
                    fontSize = 13.sp,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                var resIdx = 0
                for (match in tokenRegex.findAll(afterArrow)) {
                    if (match.range.first > resIdx) {
                        val seg = afterArrow.substring(resIdx, match.range.first).trim()
                        if (seg.isNotEmpty()) {
                            val words = seg.split(Regex("\\s+")).filter { it.isNotEmpty() }
                            words.forEach { word ->
                                val annotatedResult = remember(word) {
                                    UnicodeMathUtils.formatToAnnotatedMath(word, 13.5.sp)
                                }
                                Text(
                                    text = annotatedResult,
                                    color = Color(0xFF22C55E),
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = CambriaMathFontFamily,
                                    fontSize = 13.5.sp,
                                    modifier = Modifier.align(Alignment.CenterVertically)
                                )
                            }
                        }
                    }
                    KeycapChip(
                        keyLabel = match.groupValues[1],
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                    resIdx = match.range.last + 1
                }
                if (resIdx < afterArrow.length) {
                    val tail = afterArrow.substring(resIdx).trim()
                    if (tail.isNotEmpty()) {
                        val words = tail.split(Regex("\\s+")).filter { it.isNotEmpty() }
                        words.forEach { word ->
                            val annotatedResult = remember(word) {
                                UnicodeMathUtils.formatToAnnotatedMath(word, 13.5.sp)
                            }
                            Text(
                                text = annotatedResult,
                                color = Color(0xFF22C55E),
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = CambriaMathFontFamily,
                                fontSize = 13.5.sp,
                                modifier = Modifier.align(Alignment.CenterVertically)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Legacy helper maintained for compatibility; delegates to [CalcuTechniqueBlock].
 */
@Composable
fun KeystrokeBreadcrumbs(
    rawKeystrokes: String,
    modifier: Modifier = Modifier,
    notes: String = ""
) {
    CalcuTechniqueBlock(
        rawKeystrokes = rawKeystrokes,
        notes = notes,
        modifier = modifier
    )
}
