package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GlassyBorderBrush
import com.example.ui.theme.GlassyCardBackground
import com.example.ui.theme.GlassySpecularHighlight
import com.example.ui.theme.OrangePrimary
import com.example.ui.theme.PrimaryOrangeGradient

/**
 * Modifier that adds a fluid, tactile spring scale animation when pressed.
 */
fun Modifier.bounceClick(
    scaleDown: Float = 0.95f,
    onClick: (() -> Unit)? = null
): Modifier = composed {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleDown else 1f,
        animationSpec = spring(dampingRatio = 0.58f, stiffness = 420f),
        label = "bounce_scale"
    )

    this
        .scale(scale)
        .pointerInput(onClick) {
            awaitPointerEventScope {
                while (true) {
                    awaitFirstDown(requireUnconsumed = false)
                    isPressed = true
                    val up = waitForUpOrCancellation()
                    isPressed = false
                    if (up != null && onClick != null) {
                        onClick()
                    }
                }
            }
        }
}

/**
 * Fullscreen atmospheric background with soft warm apricot/amber spotlights
 * and fine engineering grid lines.
 */
@Composable
fun GlowingAtmosphereBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF09090C))
            .drawBehind {
                val w = size.width
                val h = size.height

                // Top Left Soft Apricot-Orange Ambient Glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x1EFB923C),
                            Color(0x0CF59E0B),
                            Color(0x00000000)
                        ),
                        center = Offset(-w * 0.1f, h * 0.18f),
                        radius = w * 0.9f
                    ),
                    center = Offset(-w * 0.1f, h * 0.18f),
                    radius = w * 0.9f
                )

                // Top Center Subtle Warm Amber
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x14FDBA74),
                            Color(0x00000000)
                        ),
                        center = Offset(w * 0.55f, -h * 0.05f),
                        radius = w * 0.7f
                    ),
                    center = Offset(w * 0.55f, -h * 0.05f),
                    radius = w * 0.7f
                )

                // Bottom Right Warm Glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x18FB923C),
                            Color(0x08EA580C),
                            Color(0x00000000)
                        ),
                        center = Offset(w * 1.15f, h * 0.82f),
                        radius = w * 0.85f
                    ),
                    center = Offset(w * 1.15f, h * 0.82f),
                    radius = w * 0.85f
                )

                // Subtle Engineering Grid Lines
                val gridStep = 42.dp.toPx()
                var x = 0f
                while (x < w) {
                    drawLine(
                        color = Color(0x06FDBA74),
                        start = Offset(x, 0f),
                        end = Offset(x, h),
                        strokeWidth = 1f
                    )
                    x += gridStep
                }
                var y = 0f
                while (y < h) {
                    drawLine(
                        color = Color(0x06FDBA74),
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 1f
                    )
                    y += gridStep
                }
            },
        content = content
    )
}

/**
 * Reusable sheer Glassmorphic Card tile with reduced opacity, soft border,
 * and delicate top sheen.
 */
@Composable
fun GlassyCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    backgroundColor: Color = GlassyCardBackground,
    borderBrush: Brush = GlassyBorderBrush,
    borderWidth: Dp = 1.dp,
    elevation: Dp = 2.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val clickableModifier = if (onClick != null) {
        Modifier.bounceClick(scaleDown = 0.97f, onClick = onClick)
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .then(clickableModifier)
            .shadow(elevation = elevation, shape = shape, spotColor = Color(0x28FB923C), ambientColor = Color(0x18000000))
            .clip(shape)
            .background(backgroundColor)
            .border(width = borderWidth, brush = borderBrush, shape = shape)
            .drawBehind {
                drawRect(
                    brush = GlassySpecularHighlight,
                    size = size.copy(height = 18.dp.toPx())
                )
            },
        content = content
    )
}

/**
 * High-impact Glassy Stat Tile component for dashboards and review HUDs.
 */
@Composable
fun GlassyStatTile(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    accentColor: Color = OrangePrimary,
    subtext: String? = null,
    onClick: (() -> Unit)? = null
) {
    GlassyCard(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = 2.dp,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.14f))
                        .border(1.dp, accentColor.copy(alpha = 0.38f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                if (subtext != null) {
                    Text(
                        text = subtext,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFA1A1AA)
                    )
                }
            }

            Column {
                Text(
                    text = value,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFA1A1AA)
                )
            }
        }
    }
}

/**
 * Glowing Glassy Pill Badge
 */
@Composable
fun GlassyPillBadge(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = OrangePrimary,
    icon: ImageVector? = null
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.14f))
            .border(1.dp, color.copy(alpha = 0.42f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(12.dp)
                )
            }
            Text(
                text = text,
                color = color,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

/**
 * High-tech Glowing Gradient Button
 */
@Composable
fun GlassyGradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    gradient: Brush = PrimaryOrangeGradient,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier
            .bounceClick(scaleDown = 0.95f, onClick = if (enabled) onClick else null)
            .shadow(
                elevation = if (enabled) 5.dp else 0.dp,
                shape = RoundedCornerShape(14.dp),
                spotColor = Color(0x55FB923C)
            )
            .clip(RoundedCornerShape(14.dp))
            .background(if (enabled) gradient else Brush.linearGradient(listOf(Color(0x3352525B), Color(0x3352525B))))
            .border(
                width = 1.dp,
                color = if (enabled) Color(0x66FDBA74) else Color(0x22FFFFFF),
                shape = RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (enabled) Color.White else Color(0xFF71717A),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                color = if (enabled) Color.White else Color(0xFF71717A),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}
