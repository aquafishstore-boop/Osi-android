package live.osirisai.app.ui.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import live.osirisai.app.R
import live.osirisai.app.ui.theme.OsirisBlack
import live.osirisai.app.ui.theme.OsirisGold
import live.osirisai.app.ui.theme.OsirisHairline
import live.osirisai.app.ui.theme.OsirisMuted
import live.osirisai.app.ui.theme.OsirisPanel
import live.osirisai.app.ui.theme.OsirisText

@Composable
fun ConnectionOverlay(
    title: String,
    body: String,
    detail: String? = null,
    onRetry: () -> Unit,
    onAbout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(OsirisBlack.copy(alpha = 0.92f)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp)
                .background(OsirisPanel, RoundedCornerShape(4.dp))
                .border(1.dp, OsirisHairline, RoundedCornerShape(4.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = OsirisGold,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = body,
                style = MaterialTheme.typography.bodyLarge,
                color = OsirisText,
                textAlign = TextAlign.Center,
            )
            if (!detail.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OsirisMuted,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = OsirisGold,
                    contentColor = OsirisBlack,
                ),
                shape = RoundedCornerShape(2.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(R.string.retry).uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = OsirisBlack,
                )
            }
            TextButton(onClick = onAbout) {
                Text(
                    text = stringResource(R.string.about).uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = OsirisMuted,
                )
            }
        }
    }
}
