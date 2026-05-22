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
    val emeraldColor = LivingTealPrimary
    val orangeColor = LivingOrangeSecondary

    val scale by animateFloatAsState(
        targetValue = if (animationTrigger) 1f else 0.85f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "LogoScale"
    )

    Box(
        modifier = modifier
            .size(100.dp)
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(LivingTealPrimary.copy(alpha = 0.25f), Color.Transparent),
                        center = center,
                        radius = size.width / 1.1f
                    )
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(64.dp * scale)) {
            val width = size.width
            val height = size.height

            // Cozy Roof Top and modern door accents
            // Outer House framing
            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(width / 2f, height * 0.15f)
                lineTo(width * 0.9f, height * 0.5f)
                lineTo(width * 0.78f, height * 0.5f)
                lineTo(width * 0.78f, height * 0.85f)
                lineTo(width * 0.22f, height * 0.85f)
                lineTo(width * 0.22f, height * 0.5f)
                lineTo(width * 0.1f, height * 0.5f)
                close()
            }
            drawPath(
                path = path,
                color = emeraldColor,
                style = Stroke(width = 4.dp.toPx())
            )

            // Inner Accent Heart / Sunset Sun indicating "Living" with care
            drawCircle(
                color = orangeColor,
                radius = 10.dp.toPx(),
                center = Offset(width / 2f, height * 0.52f)
            )

            // Dynamic base waves
            drawLine(
                color = emeraldColor.copy(alpha = 0.7f),
                start = Offset(width * 0.15f, height * 0.92f),
                end = Offset(width * 0.85f, height * 0.92f),
                strokeWidth = 3.dp.toPx()
            )
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
    if (points.isEmpty()) return
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
