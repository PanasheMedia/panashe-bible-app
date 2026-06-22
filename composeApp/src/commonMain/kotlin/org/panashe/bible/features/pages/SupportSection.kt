package org.panashe.bible.features.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.panashe.bible.ui.components.CheckIcon
import org.panashe.bible.ui.components.CopyIcon
import org.panashe.bible.ui.components.Eyebrow
import org.panashe.bible.ui.components.SectionCard

/** Crypto addresses for supporting the project — kept in lockstep with the web about page. */
private val supportWallets = listOf(
    "Bitcoin (BTC)" to "bc1qz4kg0f4l209zh49yaapjmvn0fw6svuz4cn87q7",
    "Ethereum (ETH)" to "0x3072A4F65A2B9Ba35d69f7ed2A639163451c2d52",
    "Litecoin (LTC)" to "LbR12WwMeFFoyf5hieqMtd3AyFWHJv7mdv",
    "Monero (XMR)" to "44CMKkG3QFCEXgm25SRX9bDMywFT1jUpdEbE6qCiL8kLCYf6g8QFNZTP1pDqmaP9w1FvES1fGePRuMJtYiyMubZe6qsJ2sx"
)

@Composable
fun SupportSection() {
    val clipboard = LocalClipboardManager.current
    var copiedLabel by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(copiedLabel) {
        if (copiedLabel != null) {
            delay(1600)
            copiedLabel = null
        }
    }

    SectionCard {
        Text(
            "Support the project",
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = FontFamily.Serif,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(10.dp))
        Text(
            "If this reader has served you well and you desire to help with its development, maintenance, and hosting, tap any address below to copy it.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            lineHeight = 22.sp
        )
        Spacer(Modifier.height(16.dp))
        supportWallets.forEachIndexed { index, (label, address) ->
            if (index > 0) Spacer(Modifier.height(10.dp))
            WalletRow(
                label = label,
                address = address,
                copied = copiedLabel == label,
                onCopy = {
                    clipboard.setText(AnnotatedString(address))
                    copiedLabel = label
                }
            )
        }
    }
}

@Composable
private fun WalletRow(label: String, address: String, copied: Boolean, onCopy: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onCopy)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Eyebrow(label)
                Spacer(Modifier.height(4.dp))
                Text(
                    address,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(12.dp))
            if (copied) {
                Icon(
                    CheckIcon,
                    contentDescription = "Copied",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(18.dp)
                )
            } else {
                Icon(
                    CopyIcon,
                    contentDescription = "Copy $label address",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
