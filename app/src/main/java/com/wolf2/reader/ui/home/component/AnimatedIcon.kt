package com.wolf2.reader.ui.home.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
internal fun AnimatedIcon(
    selected: Boolean,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    modifier: Modifier = Modifier
) {
    AnimatedContent(
        targetState = selected,
        transitionSpec = {
            (fadeIn(animationSpec = tween(150, 150)) +
                    scaleIn(initialScale = 0.8f, animationSpec = tween(150))) togetherWith
                    (fadeOut(animationSpec = tween(150)) +
                            scaleOut(targetScale = 0.8f, animationSpec = tween(150)))
        }
    ) { targetSelected ->
        Icon(
            imageVector = if (targetSelected) selectedIcon else unselectedIcon,
            contentDescription = null,
            modifier = modifier
        )
    }
}