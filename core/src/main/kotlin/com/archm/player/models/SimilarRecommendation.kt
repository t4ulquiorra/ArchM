

package com.archm.player.models

import com.music.innertube.models.YTItem
import com.archm.player.db.entities.LocalItem

data class SimilarRecommendation(
    val title: LocalItem,
    val items: List<YTItem>,
)
