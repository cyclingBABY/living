package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.nativeCanvas
import android.graphics.Paint
import android.graphics.Rect

// --- Custom Modern Glassmorphic Container ---
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
    borderColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
    tonalElevation: Dp = 2.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .border(1.dp, borderColor, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = tonalElevation),
        content = content
    )
}


// --- Logo Drawing with Canvas ---
@Composable
fun AppLogoCanvas(
    modifier: Modifier = Modifier,
    animationTrigger: Boolean = true
) {
    val scale by animateFloatAsState(
        targetValue = if (animationTrigger) 1f else 0.85f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "LogoScale"
    )

    Box(
        modifier = modifier
            .size(100.dp)
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val radius = minOf(width, height) / 2f
            val cx = width / 2f
            val cy = height / 2f

            // 1. Solid Red Background Circle from user uploaded logo
            drawCircle(
                color = Color(0xFFFE2B2D),
                radius = radius,
                center = Offset(cx, cy)
            )

            // Scale factor to map virtual coordinate grid of 100x100 to the real size
            val scaleFactor = radius * 2f / 100f

            // 2. Chimney (white rectangle on the right slope of the roof)
            val chimneyLeft = cx + (15f * scaleFactor)
            val chimneyRight = cx + (25f * scaleFactor)
            val chimneyTop = cy - (26f * scaleFactor)
            val chimneyBottom = cy - (5f * scaleFactor)
            
            drawRect(
                color = Color.White,
                topLeft = Offset(chimneyLeft, chimneyTop),
                size = androidx.compose.ui.geometry.Size(chimneyRight - chimneyLeft, chimneyBottom - chimneyTop)
            )

            // 3. Roof Outer Outline Structure (the elegant white /\ shape with eaves)
            val roofPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(cx - (38f * scaleFactor), cy + (1f * scaleFactor))
                lineTo(cx, cy - (33f * scaleFactor))
                lineTo(cx + (38f * scaleFactor), cy + (1f * scaleFactor))
            }
            drawPath(
                path = roofPath,
                color = Color.White,
                style = Stroke(width = 8f * scaleFactor, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // 4. House Main Body (White block with a door cutout)
            val houseLeft = cx - (26f * scaleFactor)
            val houseRight = cx + (26f * scaleFactor)
            val houseTop = cy + (1f * scaleFactor)
            val houseBottom = cy + (34f * scaleFactor)
            val bodyWidth = houseRight - houseLeft
            val bodyHeight = houseBottom - houseTop

            drawRect(
                color = Color.White,
                topLeft = Offset(houseLeft, houseTop),
                size = androidx.compose.ui.geometry.Size(bodyWidth, bodyHeight)
            )

            // 5. Red Door Cutout in bottom center
            val doorWidth = 16f * scaleFactor
            val doorHeight = 17f * scaleFactor
            val doorLeft = cx - (8f * scaleFactor)
            val doorTop = houseBottom - doorHeight

            drawRect(
                color = Color(0xFFFE2B2D),
                topLeft = Offset(doorLeft, doorTop),
                size = androidx.compose.ui.geometry.Size(doorWidth, doorHeight + 1f)
            )

            // 6. Signature circular badge on top of chimney
            val badgeX = cx + (20f * scaleFactor)
            val badgeY = cy - (32f * scaleFactor)
            val badgeRadius = 13f * scaleFactor

            drawCircle(
                color = Color.White,
                radius = badgeRadius,
                center = Offset(badgeX, badgeY)
            )

            // Multicolored Ring segments on the badge (yellow, red, black/grey)
            val arcRect = androidx.compose.ui.geometry.Rect(
                badgeX - badgeRadius + (1f * scaleFactor),
                badgeY - badgeRadius + (1f * scaleFactor),
                badgeX + badgeRadius - (1f * scaleFactor),
                badgeY + badgeRadius - (1f * scaleFactor)
            )
            
            drawArc(
                color = Color(0xFFE53935), // Red
                startAngle = -30f,
                sweepAngle = 120f,
                useCenter = false,
                topLeft = arcRect.topLeft,
                size = arcRect.size,
                style = Stroke(width = 2f * scaleFactor)
            )
            
            drawArc(
                color = Color(0xFFFFEB3B), // Yellow
                startAngle = 90f,
                sweepAngle = 120f,
                useCenter = false,
                topLeft = arcRect.topLeft,
                size = arcRect.size,
                style = Stroke(width = 2f * scaleFactor)
            )
            
            drawArc(
                color = Color(0xFF212121), // Black/Dimgrey
                startAngle = 210f,
                sweepAngle = 120f,
                useCenter = false,
                topLeft = arcRect.topLeft,
                size = arcRect.size,
                style = Stroke(width = 2f * scaleFactor)
            )

            // Drawing "c5de5" using Android Native Canvas calls inside the Compose DrawScope
            drawContext.canvas.nativeCanvas.apply {
                val density = scaleFactor

                // Big central "5"
                val paintFive = Paint().apply {
                    color = android.graphics.Color.BLACK
                    textSize = 14f * density
                    typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.BOLD)
                    isAntiAlias = true
                    textAlign = Paint.Align.CENTER
                }
                val bounds = Rect()
                paintFive.getTextBounds("5", 0, 1, bounds)
                val textHeight = bounds.height()
                drawText("5", badgeX, badgeY + (textHeight / 2f), paintFive)

                // Left "c"
                val paintC = Paint().apply {
                    color = android.graphics.Color.BLACK
                    textSize = 4.5f * density
                    typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.BOLD)
                    isAntiAlias = true
                    textAlign = Paint.Align.RIGHT
                }
                drawText("c", badgeX - (4.5f * density), badgeY + (textHeight / 2.5f), paintC)

                // Right "de5"
                val paintDe5 = Paint().apply {
                    color = android.graphics.Color.BLACK
                    textSize = 4.5f * density
                    typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.BOLD)
                    isAntiAlias = true
                    textAlign = Paint.Align.LEFT
                }
                drawText("de5", badgeX + (4.5f * density), badgeY + (textHeight / 2.5f), paintDe5)
                
                // Small gray top date "2021"
                val paintTopText = Paint().apply {
                    color = android.graphics.Color.GRAY
                    textSize = 1.8f * density
                    typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.NORMAL)
                    isAntiAlias = true
                    textAlign = Paint.Align.CENTER
                }
                drawText("2021", badgeX, badgeY - (8f * density), paintTopText)
            }
        }
    }
}

// --- Star Rating Indicator ---
@Composable
fun RatingStars(
    rating: Float,
    modifier: Modifier = Modifier,
    starColor: Color = LivingOrangeSecondary,
    starSize: Dp = 16.dp
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        val fullStars = rating.toInt()
        val hasHalf = (rating - fullStars) >= 0.5f
        for (i in 1..5) {
            val icon = when {
                i <= fullStars -> Icons.Default.Star
                i == fullStars + 1 && hasHalf -> Icons.Default.StarHalf
                else -> Icons.Default.StarBorder
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = starColor,
                modifier = Modifier.size(starSize)
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = String.format("%.1f", rating),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

// --- Beautiful Empty State Placeholder ---
@Composable
fun EmptyStatePlaceholder(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(LivingTealPrimary.copy(alpha = 0.08f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = LivingTealPrimary,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
            modifier = Modifier.padding(horizontal = 24.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

// --- Micro-Chart analytics Canvas block ---
@Composable
fun MetricLineChart(
    points: List<Float>,
    modifier: Modifier = Modifier,
    lineColor: Color = LivingTealPrimary
) {
    if (points.size <= 1) return
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val spacing = width / (points.size - 1)
        val maxVal = (points.maxOrNull() ?: 1f).coerceAtLeast(1f)
        val minVal = points.minOrNull() ?: 0f
        val range = (maxVal - minVal).coerceAtLeast(1f)

        val path = androidx.compose.ui.graphics.Path()
        val fillPath = androidx.compose.ui.graphics.Path()

        points.forEachIndexed { idx, value ->
            val ratio = (value - minVal) / range
            val x = idx * spacing
            val y = height - (ratio * (height - 30.dp.toPx())) - 15.dp.toPx()

            if (idx == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, height)
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
            if (idx == points.size - 1) {
                fillPath.lineTo(x, height)
            }
        }

        // Draw dynamic linear gradient under line
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(lineColor.copy(alpha = 0.35f), Color.Transparent),
                startY = 0f,
                endY = height
            )
        )

        // Draw actual line
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 3.dp.toPx())
        )

        // Draw small endpoints
        points.forEachIndexed { idx, value ->
            val ratio = (value - minVal) / range
            val x = idx * spacing
            val y = height - (ratio * (height - 30.dp.toPx())) - 15.dp.toPx()
            drawCircle(
                color = lineColor,
                radius = 4.dp.toPx(),
                center = Offset(x, y)
            )
            drawCircle(
                color = Color.White,
                radius = 2.dp.toPx(),
                center = Offset(x, y)
            )
        }
    }
}

@Composable
fun MetricBarChart(
    points: List<Float>,
    labels: List<String>,
    modifier: Modifier = Modifier,
    barColor: Color = LivingOracleOrange
) {
    if (points.isEmpty()) return
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            val maxVal = points.maxOrNull()?.coerceAtLeast(1f) ?: 1f
            points.forEach { pt ->
                val ratio = pt / maxVal
                Box(
                    modifier = Modifier
                        .width(22.dp)
                        .fillMaxHeight(ratio.coerceIn(0.15f, 1f))
                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(barColor, barColor.copy(alpha = 0.6f))
                            )
                        )
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            labels.forEach { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// Orange brand highlight color token
val LivingOracleOrange = Color(0xFFF16E24)
