package com.wolf2.reader.ui.theme

import android.os.Build
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.expressiveLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import com.wolf2.reader.constant.AppColor

private fun darkenColor(color: Color, factor: Float): Color {
    return lerp(color, Color.Black, factor)
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ComicReaderTheme(
    darkMode: Boolean = isSystemInDarkTheme(),
    amoledMode: Boolean = false,
    color: AppColor = AppColor.DYNAMIC,
    content: @Composable () -> Unit
) {
    var colorScheme = when (color) {
        AppColor.PURPLE -> if (darkMode) DarkPurpleColors else LightPurpleColors
        AppColor.BLUE -> if (darkMode) DarkBlueColors else LightBlueColors
        AppColor.GREEN -> if (darkMode) DarkGreenColors else LightGreenColors
        AppColor.ORANGE -> if (darkMode) DarkOrangeColors else LightOrangeColors
        AppColor.RED -> if (darkMode) DarkRedColors else LightRedColors

        else -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val context = LocalContext.current
            if (darkMode) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        } else {
            if (darkMode) darkColorScheme() else expressiveLightColorScheme()
        }
    }

    val contentTargetFactor = remember(amoledMode) { if (amoledMode) 0.1f else 0f }
    val contentDimmingFactor by animateFloatAsState(
        targetValue = contentTargetFactor,
        animationSpec = tween(durationMillis = 1000)
    )
    val backgroundTargetFactor = remember(amoledMode) { if (amoledMode) 1f else 0f }
    val backgroundDimmingFactor by animateFloatAsState(
        targetValue = backgroundTargetFactor,
        animationSpec = tween(durationMillis = 1000)
    )

    if (darkMode) colorScheme = colorScheme.copy(
        background = darkenColor(colorScheme.background, backgroundDimmingFactor),
        surface = darkenColor(colorScheme.surface, backgroundDimmingFactor),
        surfaceContainer = darkenColor(colorScheme.surfaceContainer, backgroundDimmingFactor),
        surfaceVariant = darkenColor(colorScheme.surfaceVariant, contentDimmingFactor),
        surfaceContainerLowest = darkenColor(
            colorScheme.surfaceContainerLowest, contentDimmingFactor
        ),
        surfaceContainerLow = darkenColor(
            colorScheme.surfaceContainerLow, contentDimmingFactor
        ),
        surfaceContainerHigh = darkenColor(
            colorScheme.surfaceContainerHigh, contentDimmingFactor
        ),
        surfaceContainerHighest = darkenColor(
            colorScheme.surfaceContainerHighest, contentDimmingFactor
        ),
        surfaceDim = darkenColor(colorScheme.surfaceDim, contentDimmingFactor),
        surfaceBright = darkenColor(colorScheme.surfaceBright, contentDimmingFactor),
        scrim = darkenColor(colorScheme.scrim, contentDimmingFactor),
        inverseSurface = darkenColor(colorScheme.inverseSurface, contentDimmingFactor),
        errorContainer = darkenColor(colorScheme.errorContainer, contentDimmingFactor),
        tertiaryContainer = darkenColor(colorScheme.tertiaryContainer, contentDimmingFactor),
        secondaryContainer = darkenColor(colorScheme.secondaryContainer, contentDimmingFactor),
        primaryContainer = darkenColor(colorScheme.primaryContainer, contentDimmingFactor)
    )

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        typography = Typography
    ) {
        content()
    }
}
