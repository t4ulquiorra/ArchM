package com.music.innertube.models

import kotlinx.serialization.Serializable
import com.music.innertube.utils.parseTime

@Serializable
data class Runs(
    val runs: List<Run>?,
)

@Serializable
data class Run(
    val text: String,
    val navigationEndpoint: NavigationEndpoint?,
)

private const val compactCountSuffixPattern =
    "KkMmBbTt\\u4e07\\u842c\\u5104\\u4ebf\\u5146\\u5343\\ucc9c\\ub9cc\\uc5b5"
private val countTextRegex =
    Regex("""\p{Nd}[\p{Nd}\s,.\uFF0C\uFF0E]*[$compactCountSuffixPattern]*""")
private val separatedSuffixRegex = Regex("""\s+(?=[$compactCountSuffixPattern]$)""")

fun Runs?.extractCountText(): String? {
    val texts = this?.runs
        ?.map { it.text.trim() }
        ?.filter { it.isNotEmpty() }
        .orEmpty()

    return texts
        .joinToString(separator = "")
        .extractCountValue()
        ?: texts.firstNotNullOfOrNull { it.extractCountValue() }
}

private fun String.extractCountValue(): String? =
    countTextRegex.find(this)
        ?.value
        ?.trim()
        ?.replace(separatedSuffixRegex, "")
        ?.takeIf { value -> value.any { it.isDigit() } }

fun List<Run>.splitBySeparator(): List<List<Run>> {
    val res = mutableListOf<List<Run>>()
    var tmp = mutableListOf<Run>()
    forEach { run ->
        if (run.text == " • ") {
            res.add(tmp)
            tmp = mutableListOf()
        } else {
            tmp.add(run)
        }
    }
    res.add(tmp)
    return res
}

fun List<List<Run>>.clean(): List<List<Run>> =
    if (getOrNull(0)?.getOrNull(0)?.navigationEndpoint != null ||
        (getOrNull(0)?.getOrNull(0)?.text?.contains(regex = Regex("[&,]"))) != false
    ) {
        this
    } else {
        this.drop(1)
    }

fun List<Run>.oddElements() =
    filterIndexed { index, _ ->
        index % 2 == 0
    }

private val ViewCountRegex = Regex("""([\d.,]+)\s*([KMB])\s*(plays?|views?)?""", RegexOption.IGNORE_CASE)
private val ViewsWordRegex = Regex("""([\d.,]+)\s*(plays?|views?)""", RegexOption.IGNORE_CASE)

fun parseViewCount(text: String): Long? {
    if (text.contains(":")) return null
    val match = ViewCountRegex.find(text) ?: ViewsWordRegex.find(text) ?: return null
    val numberText = match.groupValues[1]
    val suffix = if (match.groupValues.size > 2) match.groupValues[2].uppercase() else ""
    val value =
        if (suffix.isNotEmpty() && suffix in listOf("K", "M", "B")) {
            numberText.replace(',', '.').toDoubleOrNull()
        } else {
            numberText.filter(Char::isDigit).toDoubleOrNull()
        } ?: return null
    val multiplier =
        when (suffix) {
            "K" -> 1_000.0
            "M" -> 1_000_000.0
            "B" -> 1_000_000_000.0
            else -> 1.0
        }
    return (value * multiplier).toLong()
}

fun List<List<Run>>.viewCountText(): String? =
    firstNotNullOfOrNull { group ->
        val text = group.joinToString(separator = "") { it.text }.trim().removePrefix("•").trim()
        text.takeIf {
            group.none { run -> run.navigationEndpoint != null } &&
                !it.contains(":") &&
                it.parseTime() == null &&
                it.toIntOrNull()?.let { value -> value !in 1900..2100 } != false &&
                parseViewCount(it) != null
        }
    }

fun List<List<Run>>.viewCount(): Long? = viewCountText()?.let(::parseViewCount)
