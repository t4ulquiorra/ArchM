

package com.archm.player.ui.utils

import java.text.DecimalFormat
import kotlin.math.absoluteValue
import kotlin.math.floor

fun formatFileSize(sizeBytes: Long): String {
    val prefix = if (sizeBytes < 0) "-" else ""
    var result: Long = sizeBytes.absoluteValue
    var suffix = "B"
    if (result > 900) {
        suffix = "KB"
        result /= 1024
    }
    if (result > 900) {
        suffix = "MB"
        result /= 1024
    }
    if (result > 900) {
        suffix = "GB"
        result /= 1024
    }
    if (result > 900) {
        suffix = "TB"
        result /= 1024
    }
    if (result > 900) {
        suffix = "PB"
        result /= 1024
    }
    return "$prefix$result $suffix"
}

fun numberFormatter(n: Int) =
    DecimalFormat("#,###")
        .format(n)
        .replace(",", ".")

fun formatCompactCount(count: Long): String {
    val abs = count.absoluteValue
    val prefix = if (count < 0) "-" else ""

    fun compactOneDecimal(divisor: Long): String {
        val value = floor(abs.toDouble() / (divisor / 10.0)) / 10.0
        val text = DecimalFormat("#.#").format(value).replace(",", ".")
        return if (text.endsWith(".0")) text.dropLast(2) else text
    }

    return when {
        abs < 1_000 -> "$count"
        abs < 10_000 -> prefix + compactOneDecimal(1_000) + "K"
        abs < 1_000_000 -> prefix + (abs / 1_000) + "K"
        abs < 10_000_000 -> prefix + compactOneDecimal(1_000_000) + "M"
        abs < 1_000_000_000 -> prefix + (abs / 1_000_000) + "M"
        abs < 10_000_000_000 -> prefix + compactOneDecimal(1_000_000_000) + "B"
        else -> prefix + (abs / 1_000_000_000) + "B"
    }
}
