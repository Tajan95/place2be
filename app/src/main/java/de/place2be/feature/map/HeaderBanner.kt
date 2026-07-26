package de.place2be.feature.map

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.place2be.ui.theme.DarkInk
import kotlinx.coroutines.delay

@Composable
internal fun HeaderBanner(
    modifier: Modifier = Modifier,
) {
    val items = remember { buildHeaderBannerItems() }
    val reducedMotion = reducedMotionRequested()
    val density = LocalDensity.current.density
    val flipRotation = remember { Animatable(0f) }
    var targetIndex by rememberSaveable { mutableIntStateOf(0) }
    var displayedIndex by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(items.size, reducedMotion) {
        targetIndex = targetIndex.coerceIn(items.indices)
        displayedIndex = displayedIndex.coerceIn(items.indices)
        if (reducedMotion) {
            targetIndex = 0
            displayedIndex = 0
            flipRotation.snapTo(0f)
            return@LaunchedEffect
        }
        if (items.size <= 1) return@LaunchedEffect

        while (true) {
            delay(HEADER_BANNER_ROTATION_MILLIS)
            targetIndex = nextHeaderBannerIndex(
                currentIndex = targetIndex,
                itemCount = items.size,
                rotationEnabled = true,
            )
        }
    }

    LaunchedEffect(targetIndex, reducedMotion) {
        if (reducedMotion) {
            displayedIndex = 0
            flipRotation.snapTo(0f)
            return@LaunchedEffect
        }
        if (displayedIndex == targetIndex) return@LaunchedEffect

        flipRotation.animateTo(
            targetValue = -FLIP_HALF_TURN_DEGREES,
            animationSpec = tween(
                durationMillis = FLIP_HALF_DURATION_MILLIS,
                easing = FastOutLinearInEasing,
            ),
        )
        displayedIndex = targetIndex
        flipRotation.snapTo(FLIP_HALF_TURN_DEGREES)
        flipRotation.animateTo(
            targetValue = 0f,
            animationSpec = tween(
                durationMillis = FLIP_HALF_DURATION_MILLIS,
                easing = LinearOutSlowInEasing,
            ),
        )
    }

    val item = items[displayedIndex.coerceIn(items.indices)]
    val palette = item.palette.colors()
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .graphicsLayer {
                rotationX = flipRotation.value
                cameraDistance = FLIP_CAMERA_DISTANCE_DP * density
                transformOrigin = TransformOrigin.Center
            }
            .semantics(mergeDescendants = true) {
                contentDescription = "${item.label}: ${item.message}"
            },
        shape = RoundedCornerShape(13.dp),
        color = palette.background,
        contentColor = DarkInk,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
            ) {
                Text(
                    text = item.label.uppercase(),
                    color = palette.accent,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    lineHeight = 9.sp,
                    maxLines = 1,
                )
                AutoFittingBannerMessage(
                    message = item.message,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun AutoFittingBannerMessage(
    message: String,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier) {
        val textMeasurer = rememberTextMeasurer()
        val density = LocalDensity.current
        val availableWidthPx = with(density) { maxWidth.roundToPx() }
        val availableHeightPx = with(density) { maxHeight.roundToPx() }
        val fittedFontSize = remember(
            message,
            availableWidthPx,
            availableHeightPx,
            density.fontScale,
            textMeasurer,
        ) {
            if (availableWidthPx <= 0 || availableHeightPx <= 0) {
                MIN_BANNER_MESSAGE_FONT_SIZE_SP
            } else {
                BANNER_MESSAGE_FONT_SIZE_CANDIDATES.firstOrNull { candidate ->
                    val layout = textMeasurer.measure(
                        text = AnnotatedString(message),
                        style = TextStyle(
                            fontSize = candidate.sp,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = (candidate + BANNER_MESSAGE_LINE_HEIGHT_EXTRA_SP).sp,
                        ),
                        overflow = TextOverflow.Clip,
                        softWrap = true,
                        maxLines = BANNER_MESSAGE_MAX_LINES,
                        constraints = Constraints(
                            maxWidth = availableWidthPx,
                            maxHeight = availableHeightPx,
                        ),
                    )
                    !layout.hasVisualOverflow
                } ?: MIN_BANNER_MESSAGE_FONT_SIZE_SP
            }
        }

        Text(
            text = message,
            modifier = Modifier.fillMaxSize(),
            color = DarkInk,
            fontSize = fittedFontSize.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = (fittedFontSize + BANNER_MESSAGE_LINE_HEIGHT_EXTRA_SP).sp,
            maxLines = BANNER_MESSAGE_MAX_LINES,
            overflow = TextOverflow.Clip,
        )
    }
}

@Composable
private fun reducedMotionRequested(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        runCatching {
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                DEFAULT_ANIMATOR_DURATION_SCALE,
            ) == 0f
        }.getOrDefault(false)
    }
}

private fun HeaderBannerPalette.colors(): HeaderBannerColors = when (this) {
    HeaderBannerPalette.CYAN -> HeaderBannerColors(
        background = Color(0xFFE3F7FA),
        accent = Color(0xFF17899A),
    )
    HeaderBannerPalette.PINK -> HeaderBannerColors(
        background = Color(0xFFFFE5F1),
        accent = Color(0xFFB13C75),
    )
}

private data class HeaderBannerColors(
    val background: Color,
    val accent: Color,
)

internal const val HEADER_BANNER_ROTATION_MILLIS = 15_000L
private const val MAX_BANNER_MESSAGE_FONT_SIZE_SP = 11f
private const val MIN_BANNER_MESSAGE_FONT_SIZE_SP = 5f
private const val BANNER_MESSAGE_FONT_SIZE_STEP_SP = 0.5f
private const val BANNER_MESSAGE_LINE_HEIGHT_EXTRA_SP = 2f
private const val BANNER_MESSAGE_MAX_LINES = 2
private val BANNER_MESSAGE_FONT_SIZE_CANDIDATES: List<Float> = buildList {
    var candidate = MAX_BANNER_MESSAGE_FONT_SIZE_SP
    while (candidate >= MIN_BANNER_MESSAGE_FONT_SIZE_SP) {
        add(candidate)
        candidate -= BANNER_MESSAGE_FONT_SIZE_STEP_SP
    }
}
private const val FLIP_HALF_DURATION_MILLIS = 600
private const val FLIP_HALF_TURN_DEGREES = 90f
private const val FLIP_CAMERA_DISTANCE_DP = 18f
private const val DEFAULT_ANIMATOR_DURATION_SCALE = 1f
