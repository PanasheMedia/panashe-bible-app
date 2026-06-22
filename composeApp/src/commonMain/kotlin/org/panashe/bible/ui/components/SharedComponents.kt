package org.panashe.bible.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.panashe.bible.ui.Accent
import org.panashe.bible.ui.Ink
import org.panashe.bible.ui.Line
import org.panashe.bible.ui.Muted

@Composable
fun LoadingText(text: String) {
    val transition = rememberInfiniteTransition(label = "loading")
    val anim by transition.animateFloat(
        initialValue = 0.6f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(750), RepeatMode.Reverse),
        label = "pulse"
    )
    Text(text, color = Muted, fontSize = 14.sp, lineHeight = 24.sp, modifier = Modifier.alpha(anim))
}

@Composable
fun Hero(eyebrow: String?, title: String, intro: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 28.dp, bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (eyebrow != null) {
            Eyebrow(eyebrow)
            Spacer(Modifier.height(7.dp))
        }
        Text(
            title,
            color = Ink,
            fontFamily = FontFamily.Serif,
            fontSize = 45.sp,
            lineHeight = 46.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-2).sp,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(14.dp))
        Text(intro, color = Muted, fontSize = 15.sp, lineHeight = 25.sp, textAlign = TextAlign.Center, modifier = Modifier.widthIn(max = 480.dp))
    }
}

@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    entranceDelayMillis: Int = 0,
    content: @Composable ColumnScope.() -> Unit
) {
    StaggeredEntrance(delayMillis = entranceDelayMillis) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, Line),
            shape = RoundedCornerShape(4.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = modifier.fillMaxWidth().padding(bottom = 30.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 26.dp), content = content)
        }
    }
}

@Composable
fun StaggeredEntrance(delayMillis: Int = 0, content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(delayMillis) {
        if (delayMillis > 0) delay(delayMillis.toLong())
        visible = true
    }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(260),
        label = "entrance-alpha"
    )
    val offsetY by animateDpAsState(
        targetValue = if (visible) 0.dp else 10.dp,
        animationSpec = tween(260),
        label = "entrance-offset"
    )
    Box(modifier = Modifier.alpha(alpha).offset(y = offsetY)) {
        content()
    }
}

@Composable
fun Eyebrow(text: String) {
    Text(
        text = text.uppercase(),
        color = Accent,
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.4.sp
    )
}

@Composable
fun PrimaryAction(label: String, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    Button(
        onClick = onClick,
        interactionSource = interactionSource,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(3.dp),
        modifier = Modifier.height(44.dp).pressScale(interactionSource)
    ) {
        Text(label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}

@Composable
fun ToastBar(message: String, visible: Boolean, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(250)) + slideInVertically(
            animationSpec = tween(250),
            initialOffsetY = { it / 2 }
        ),
        exit = fadeOut(animationSpec = tween(180)) + slideOutVertically(
            animationSpec = tween(180),
            targetOffsetY = { it / 2 }
        ),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Surface(
                color = Accent,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    message,
                    color = androidx.compose.ui.graphics.Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }
    }
}

@Composable
fun SecondaryAction(label: String, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    OutlinedButton(
        onClick = onClick,
        interactionSource = interactionSource,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)),
        shape = RoundedCornerShape(3.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
        modifier = Modifier.height(44.dp).pressScale(interactionSource)
    ) {
        Text(label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}

@Composable
fun Modifier.pressScale(interactionSource: MutableInteractionSource): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = tween(100),
        label = "press-scale"
    )
    return this.scale(scale)
}
