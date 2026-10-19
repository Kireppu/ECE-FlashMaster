package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CambriaMathFontFamily
import com.example.util.UnicodeMathUtils

/**
 * AST Node representation for 2D Built-Up Equations (fractions, radicals, and text/scripts).
 */
private sealed interface MathNode {
    data class TextSegment(val text: String) : MathNode
    data class Fraction(val numerator: List<MathNode>, val denominator: List<MathNode>) : MathNode
    data class Radical(val radicand: List<MathNode>) : MathNode
}

/**
 * Parses a math expression into a tree of [MathNode]s supporting multiple
 * `\frac{num}{den}`, `\dfrac`, `\tfrac`, `\cfrac`, and nested `\sqrt{...}` blocks.
 */
private fun parseMathNodes(raw: String): List<MathNode> {
    val input = UnicodeMathUtils.stripLatexWrappers(raw).trim()
    val nodes = mutableListOf<MathNode>()
    val buffer = StringBuilder()
    var i = 0

    fun flushBuffer() {
        if (buffer.isNotEmpty()) {
            nodes.add(MathNode.TextSegment(buffer.toString()))
            buffer.clear()
        }
    }

    while (i < input.length) {
        val fracCmd = when {
            input.startsWith("\\frac", i) -> "\\frac"
            input.startsWith("\\dfrac", i) -> "\\dfrac"
            input.startsWith("\\tfrac", i) -> "\\tfrac"
            input.startsWith("\\cfrac", i) -> "\\cfrac"
            else -> null
        }
        if (fracCmd != null) {
            var cursor = i + fracCmd.length
            while (cursor < input.length && input[cursor].isWhitespace()) cursor++
            val numGroup = UnicodeMathUtils.extractBracedGroup(input, cursor)
            if (numGroup != null) {
                cursor = numGroup.second
                while (cursor < input.length && input[cursor].isWhitespace()) cursor++
                val denGroup = UnicodeMathUtils.extractBracedGroup(input, cursor)
                if (denGroup != null) {
                    flushBuffer()
                    nodes.add(
                        MathNode.Fraction(
                            numerator = parseMathNodes(numGroup.first),
                            denominator = parseMathNodes(denGroup.first)
                        )
                    )
                    i = denGroup.second
                    continue
                }
            }
        } else if (input.startsWith("\\sqrt", i)) {
            var cursor = i + 5
            while (cursor < input.length && input[cursor].isWhitespace()) cursor++
            val radGroup = UnicodeMathUtils.extractBracedGroup(input, cursor)
            if (radGroup != null) {
                flushBuffer()
                nodes.add(MathNode.Radical(radicand = parseMathNodes(radGroup.first)))
                i = radGroup.second
                continue
            }
        }

        buffer.append(input[i])
        i++
    }
    flushBuffer()
    return nodes
}

/**
 * WordMathView: 100% Pure Native Jetpack Compose 2D Math Equation Renderer
 * styled with Cambria Math typography.
 * Renders stacked vertical fractions, dynamic radicals with overline vinculums,
 * true subscripts/superscripts, and Greek symbols without WebViews or MESA GPU dependencies.
 */
@Composable
fun WordMathView(
    text: String,
    modifier: Modifier = Modifier,
    isBlock: Boolean = false,
    fontSizeSp: Float = 16f
) {
    val nodes = remember(text) { parseMathNodes(text) }

    if (isBlock) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            SelectionContainer {
                MathNodeRow(nodes = nodes, fontSizeSp = fontSizeSp)
            }
        }
    } else {
        SelectionContainer(modifier = modifier) {
            val annotated = remember(text, fontSizeSp) {
                UnicodeMathUtils.formatToAnnotatedMath(text, fontSizeSp.sp, CambriaMathFontFamily)
            }
            Text(
                text = annotated,
                color = Color(0xFFF4F4F5),
                fontFamily = CambriaMathFontFamily,
                fontSize = fontSizeSp.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = (fontSizeSp * 1.4f).sp
            )
        }
    }
}

@Composable
private fun MathNodeRow(
    nodes: List<MathNode>,
    fontSizeSp: Float
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        nodes.forEach { node ->
            when (node) {
                is MathNode.TextSegment -> {
                    val annotated = remember(node.text, fontSizeSp) {
                        UnicodeMathUtils.formatToAnnotatedMath(node.text, fontSizeSp.sp, CambriaMathFontFamily)
                    }
                    Text(
                        text = annotated,
                        color = Color(0xFFF4F4F5),
                        fontFamily = CambriaMathFontFamily,
                        fontSize = fontSizeSp.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = (fontSizeSp * 1.3f).sp
                    )
                }

                is MathNode.Fraction -> {
                    val childFontSize = (fontSizeSp * 0.90f).coerceAtLeast(12f)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .width(IntrinsicSize.Max)
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            MathNodeRow(nodes = node.numerator, fontSizeSp = childFontSize)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.2.dp)
                                .background(Color(0xFFE4E4E7))
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier.padding(horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            MathNodeRow(nodes = node.denominator, fontSizeSp = childFontSize)
                        }
                    }
                }

                is MathNode.Radical -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 2.dp)
                    ) {
                        Text(
                            text = "√",
                            color = Color(0xFFF4F4F5),
                            fontFamily = CambriaMathFontFamily,
                            fontSize = (fontSizeSp * 1.25f).sp,
                            fontWeight = FontWeight.Normal
                        )
                        Box(
                            modifier = Modifier
                                .drawBehind {
                                    drawLine(
                                        color = Color(0xFFE4E4E7),
                                        start = Offset(0f, 1.dp.toPx()),
                                        end = Offset(size.width, 1.dp.toPx()),
                                        strokeWidth = 1.2.dp.toPx()
                                    )
                                }
                                .padding(top = 3.dp, start = 2.dp, end = 3.dp)
                        ) {
                            MathNodeRow(nodes = node.radicand, fontSizeSp = fontSizeSp * 0.94f)
                        }
                    }
                }
            }
        }
    }
}
