package com.archm.player.ui.component

import android.os.Build
import android.text.Html
import android.text.method.LinkMovementMethod
import android.util.TypedValue
import android.widget.TextView
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    color: Color = MaterialTheme.colorScheme.onSurface,
) {
    val context = LocalContext.current
    val colorArgb = color.toArgb()
    val fontSize = style.fontSize

    val spanned = remember(markdown) {
        val html = markdownToHtml(markdown)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT)
        } else {
            @Suppress("DEPRECATION")
            Html.fromHtml(html)
        }
    }

    AndroidView(
        factory = { ctx ->
            TextView(ctx).apply {
                movementMethod = LinkMovementMethod.getInstance()
            }
        },
        update = { textView ->
            textView.setTextColor(colorArgb)
            if (fontSize.isSp) {
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize.value)
            }
            textView.text = spanned
        },
        modifier = modifier,
    )
}

private fun markdownToHtml(markdown: String): String {
    val lines = markdown.lines()
    val sb = StringBuilder()
    for (line in lines) {
        val l = line.trimEnd()
        when {
            l.startsWith("### ") -> sb.append("<h3>").append(formatInline(l.removePrefix("### "))).append("</h3>")
            l.startsWith("## ") -> sb.append("<h2>").append(formatInline(l.removePrefix("## "))).append("</h2>")
            l.startsWith("# ") -> sb.append("<h1>").append(formatInline(l.removePrefix("# "))).append("</h1>")
            l.startsWith("- ") || l.startsWith("* ") -> sb.append("&bull; ").append(formatInline(l.removePrefix("- ").removePrefix("* "))).append("<br/>")
            l.isBlank() -> sb.append("<br/>")
            else -> sb.append(formatInline(l)).append("<br/>")
        }
    }
    return sb.toString()
}

private fun formatInline(text: String): String {
    var result = text
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
    result = result.replace(Regex("\\[([^\\]]+)\\]\\(([^\\)]+)\\)")) { match ->
        val title = match.groupValues[1]
        val url = match.groupValues[2]
        "<a href=\"$url\">$title</a>"
    }
    result = result.replace(Regex("\\*\\*(.+?)\\*\\*")) { "<b>${it.groupValues[1]}</b>" }
    result = result.replace(Regex("(?<!\\*)\\*(?!\\*)(.+?)(?<!\\*)\\*(?!\\*)")) { "<i>${it.groupValues[1]}</i>" }
    result = result.replace(Regex("`(.+?)`")) { "<tt>${it.groupValues[1]}</tt>" }
    return result
}
