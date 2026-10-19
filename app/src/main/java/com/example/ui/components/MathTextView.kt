package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CambriaMathFontFamily
import com.example.ui.theme.EmeraldTertiary
import com.example.ui.theme.OrangePrimary
import com.example.util.UnicodeMathUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private sealed interface MathContentBlock {
    data class Paragraph(val text: String) : MathContentBlock
    data class EquationBlock(val equation: String) : MathContentBlock
}

private fun splitMathContentBlocks(raw: String): List<MathContentBlock> {
    val result = mutableListOf<MathContentBlock>()
    // Normalize LaTeX display math to [EQ]...[/EQ]
    var normalized = raw
        .replace(Regex("""\$\$([\s\S]*?)\$\$""")) { "[EQ]" + it.groupValues[1].trim() + "[/EQ]" }
        .replace(Regex("""\\\[([\s\S]*?)\\\]""")) { "[EQ]" + it.groupValues[1].trim() + "[/EQ]" }
        .replace(Regex("""\\begin\{(?:equation|align|gather|cases|aligned|matrix|pmatrix|array)\*?\}([\s\S]*?)\\end\{(?:equation|align|gather|cases|aligned|matrix|pmatrix|array)\*?\}""")) {
            "[EQ]" + it.groupValues[1].trim() + "[/EQ]"
        }


    // If there are still no [EQ] tags, detect standalone lines containing fractions or radicals
    if (!normalized.contains("[EQ]")) {
        val lines = normalized.lines()
        val hasEquationLines = lines.any { line ->
            val t = line.trim()
            (t.contains("\\frac") || t.contains("\\dfrac") || t.contains("\\sqrt")) && !t.startsWith("Step", ignoreCase = true)
        }
        if (hasEquationLines) {
            val rebuilt = StringBuilder()
            for (line in lines) {
                val t = line.trim()
                if ((t.contains("\\frac") || t.contains("\\dfrac") || t.contains("\\sqrt")) && !t.startsWith("Step", ignoreCase = true)) {
                    rebuilt.append("[EQ]").append(t).append("[/EQ]\n")
                } else {
                    rebuilt.append(line).append("\n")
                }
            }
            normalized = rebuilt.toString()
        }
    }

    val eqRegex = Regex("\\[EQ\\]([\\s\\S]*?)\\[/EQ\\]")
    var lastIndex = 0

    for (match in eqRegex.findAll(normalized)) {
        if (match.range.first > lastIndex) {
            val before = normalized.substring(lastIndex, match.range.first).trim()
            if (before.isNotEmpty()) {
                result.add(MathContentBlock.Paragraph(before))
            }
        }
        val eqContent = match.groupValues[1].trim()
        if (eqContent.isNotEmpty()) {
            result.add(MathContentBlock.EquationBlock(eqContent))
        }
        lastIndex = match.range.last + 1
    }

    if (lastIndex < normalized.length) {
        val remaining = normalized.substring(lastIndex).trim()
        if (remaining.isNotEmpty()) {
            result.add(MathContentBlock.Paragraph(remaining))
        }
    }

    if (result.isEmpty() && raw.isNotBlank()) {
        result.add(MathContentBlock.Paragraph(raw.trim()))
    }
    return result
}

/**
 * Renders mathematical expressions and mixed engineering text:
 * - Standalone `[EQ]...[/EQ]` blocks or pure formulas with `\frac`/`\sqrt` render via 2D [WordMathView].
 * - Paragraphs and inline `[IEQ]...[/IEQ]` segments render as wrapped selectable text with
 *   true subscripts, superscripts, and Greek symbols via [UnicodeMathUtils.formatToAnnotatedMath].
 */
@Composable
fun MathTextView(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    fontWeight: FontWeight = FontWeight.Normal,
    isEquationBlock: Boolean = false
) {
    val blocks = remember(text) { splitMathContentBlocks(text) }
    val baseFontSize = if (style.fontSize.value > 0f) style.fontSize else 15.sp

    SelectionContainer(modifier = modifier) {
        if (isEquationBlock) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x4018181F))
                    .border(
                        width = 1.dp,
                        color = Color(0x28FDBA74),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    blocks.forEach { block ->
                        when (block) {
                            is MathContentBlock.Paragraph -> {
                                val annotated = remember(block.text, baseFontSize) {
                                    UnicodeMathUtils.formatToAnnotatedMath(block.text, baseFontSize, CambriaMathFontFamily)
                                }
                                Text(
                                    text = annotated,
                                    style = style.copy(
                                        fontWeight = fontWeight,
                                        lineHeight = (baseFontSize.value * 1.45f).sp
                                    ),
                                    color = Color(0xFFE4E4E7)
                                )
                            }

                            is MathContentBlock.EquationBlock -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0x66101014))
                                        .border(1.dp, Color(0x22FDBA74), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    WordMathView(
                                        text = block.equation,
                                        isBlock = true,
                                        fontSizeSp = (baseFontSize.value + 1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                blocks.forEach { block ->
                    when (block) {
                        is MathContentBlock.Paragraph -> {
                            val annotated = remember(block.text, baseFontSize) {
                                UnicodeMathUtils.formatToAnnotatedMath(block.text, baseFontSize, CambriaMathFontFamily)
                            }
                            Text(
                                text = annotated,
                                style = style.copy(
                                    fontWeight = fontWeight,
                                    lineHeight = (baseFontSize.value * 1.45f).sp
                                ),
                                color = Color(0xFFF4F4F5)
                            )
                        }

                        is MathContentBlock.EquationBlock -> {
                            WordMathView(
                                text = block.equation,
                                isBlock = true,
                                fontSizeSp = baseFontSize.value
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Formula display card rendering Word-style 2D built-up equations with Cambria Math typography
 * and one-tap copy action.
 */
@Composable
fun FormulaDisplayCard(
    name: String,
    formula: String,
    variables: String = "",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isCopied by remember { mutableStateOf(false) }

    GlassyCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0x22FB923C))
                        .border(1.dp, Color(0x44FB923C), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "FORMULA",
                        color = OrangePrimary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = name,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("ECE Formula", "$name: $formula"))
                        isCopied = true
                        scope.launch {
                            delay(2000)
                            isCopied = false
                        }
                    },
                    modifier = Modifier.bounceClick(scaleDown = 0.9f)
                ) {
                    if (isCopied) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Copied",
                            tint = EmeraldTertiary,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy formula",
                            tint = OrangePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 2D Built-up Equation Renderer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0x660F0E13))
                    .border(1.dp, Color(0x24FDBA74), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                KatexMathView(
                    mathContent = formula,
                    forceBlock = true,
                    fontSizeSp = 17f,
                    minHeight = 44.dp,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (variables.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                val formattedVars = remember(variables) {
                    UnicodeMathUtils.formatToAnnotatedMath(variables, 12.sp, CambriaMathFontFamily)
                }
                Text(
                    text = formattedVars,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFA1A1AA)
                )
            }
        }
    }
}
