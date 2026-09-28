

package com.archm.player.ui.utils

fun String.resize(
    width: Int? = null,
    height: Int? = null,
): String {
    if (width == null && height == null) return this

    
    
    
    
    if (this.contains("i.ytimg.com")) {
        return this
    }

    
    if (this.contains("googleusercontent.com") && this.contains("=w")) {
        val baseUrl = this.split("=w")[0]
        if (width != null && height != null) {
            return "$baseUrl=w$width-h$height"
        }
        val size = if ((width ?: 0) >= 1000 || (height ?: 0) >= 1000) 1200 else 500
        return "$baseUrl=w$size-h$size"
    }

    if (this.contains("yt3.ggpht.com")) {
        val baseUrl = this.split("=")[0].split("-s")[0]
        return "$baseUrl=s${width ?: height}"
    }

    "https://lh\\d\\.googleusercontent\\.com/.*".toRegex().matchEntire(this)?.let {
        if (width != null && height != null) {
            return "${this.split("=")[0]}=w$width-h$height"
        }
        val size = if ((width ?: 0) >= 1000 || (height ?: 0) >= 1000) 1200 else 500
        return "${this.split("=")[0]}=w$size-h$size"
    }

    return this
}
