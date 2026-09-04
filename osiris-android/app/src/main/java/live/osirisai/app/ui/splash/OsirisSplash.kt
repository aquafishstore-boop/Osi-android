package live.osirisai.app.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import live.osirisai.app.R
import live.osirisai.app.ui.theme.OsirisBlack
import live.osirisai.app.ui.theme.OsirisGold
import live.osirisai.app.ui.theme.OsirisMuted
import live.osirisai.app.ui.theme.OsirisText
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun OsirisSplash(
    statusText: String = stringResource(R.string.connecting),
    modifier: Modifier = Modifier,
) {
    val pulse = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        pulse.animateTo(
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(2200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(OsirisBlack)
            .semantics { contentDescription = statusText },
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Canvas(modifier = Modifier.size(120.dp)) {
                val stroke = 2.dp.toPx()
                val radius = size.minDimension / 2f - stroke
                drawCircle(
                    color = OsirisText.copy(alpha = 0.55f),
                    radius = radius,
                    style = Stroke(width = stroke),
                )
                val angle = pulse.value * Math.PI.toFloat() * 2f - (Math.PI / 2f).toFloat()
                val cx = center.x + radius * cos(angle)
                val cy = center.y + radius * sin(angle)
                drawCircle(color = OsirisGold, radius = 5.dp.toPx(), center = Offset(cx, cy))
                // faint star points
                drawCircle(color = OsirisMuted, radius = 2.dp.toPx(), center = Offset(center.x - 18.dp.toPx(), center.y - 6.dp.toPx()))
                drawCircle(color = OsirisMuted, radius = 1.5.dp.toPx(), center = Offset(center.x + 14.dp.toPx(), center.y + 4.dp.toPx()))
                drawCircle(color = OsirisMuted, radius = 1.2.dp.toPx(), center = Offset(center.x - 4.dp.toPx(), center.y + 16.dp.toPx()))
            }
            Spacer(Modifier.height(28.dp))
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.displayLarge,
                color = OsirisText,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.splash_tagline).uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = OsirisMuted,
            )
            Spacer(Modifier.height(36.dp))
            Canvas(modifier = Modifier.width(120.dp).height(2.dp)) {
                val progress = pulse.value
                drawLine(
                    color = OsirisGold.copy(alpha = 0.35f),
                    start = Offset(0f, size.height / 2),
                    end = Offset(size.width, size.height / 2),
                    strokeWidth = size.height,
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = OsirisGold,
                    start = Offset(0f, size.height / 2),
                    end = Offset(size.width * progress, size.height / 2),
                    strokeWidth = size.height,
                    cap = StrokeCap.Round,
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = statusText.uppercase(),
                style = MaterialTheme.typography.titleMedium,
                color = OsirisMuted,
            )
        }
    }
}
