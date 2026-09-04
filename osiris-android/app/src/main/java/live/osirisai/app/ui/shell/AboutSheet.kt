package live.osirisai.app.ui.shell

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import live.osirisai.app.BuildConfig
import live.osirisai.app.R
import live.osirisai.app.ui.theme.OsirisBlack
import live.osirisai.app.ui.theme.OsirisCharcoal
import live.osirisai.app.ui.theme.OsirisGold
import live.osirisai.app.ui.theme.OsirisMuted
import live.osirisai.app.ui.theme.OsirisText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutSheet(
    onDismiss: () -> Unit,
    onOpenDocs: () -> Unit,
    onClearCache: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = OsirisCharcoal,
        contentColor = OsirisText,
        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 8.dp),
        ) {
            Text(
                text = stringResource(R.string.about_title).uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = OsirisGold,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(
                    R.string.version_fmt,
                    BuildConfig.VERSION_NAME,
                    BuildConfig.VERSION_CODE,
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = OsirisMuted,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.about_body),
                style = MaterialTheme.typography.bodyLarge,
                color = OsirisText,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.about_attribution),
                style = MaterialTheme.typography.bodyMedium,
                color = OsirisMuted,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.about_ethics),
                style = MaterialTheme.typography.bodyMedium,
                color = OsirisMuted,
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onOpenDocs,
                colors = ButtonDefaults.buttonColors(
                    containerColor = OsirisGold,
                    contentColor = OsirisBlack,
                ),
                shape = RoundedCornerShape(2.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(R.string.open_docs).uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = OsirisBlack,
                )
            }
            TextButton(
                onClick = onClearCache,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(R.string.clear_cache).uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = OsirisMuted,
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
