package it.dogior.hadEnough

import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.fixUrl
import com.lagradost.cloudstream3.SubtitleFile

class AnimeSaturnExtractor : ExtractorApi() {
    override val name = "AnimeSaturn"
    override val mainUrl = "https://www.animesaturn.cx"
    override val requiresReferer = true

    override suspend fun getUrl(
        url: String,
        referer: String?,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {
        val timeout = 60L
        
        try {
            val episodeDoc = app.get(url, timeout = timeout).document
            
            val watchLink = episodeDoc.select("a[href*='/watch?file=']").attr("href")
            if (watchLink.isBlank()) return
            
            val watchUrl = fixUrl(watchLink)
            val playerDoc = app.get(watchUrl, timeout = timeout).document
            
            val videoUrl = playerDoc.select("video source").attr("src")
            if (videoUrl.isNotBlank()) {
                callback.invoke(
                    newExtractorLink(
                        source = this.name,
                        name = "AnimeSaturn",
                        url = videoUrl,
                        type = ExtractorLinkType.VIDEO
                    ) {
                        this.quality = 1080
                        this.referer = mainUrl
                    }
                )
                return
            }
            
            val altPlayerLink = episodeDoc.select("a[href*='&s=alt']").attr("href")
            if (altPlayerLink.isNotBlank()) {
                val altUrl = fixUrl(altPlayerLink)
                val altDoc = app.get(altUrl, timeout = timeout).document
                val altVideoUrl = altDoc.select("video source").attr("src")
                
                if (altVideoUrl.isNotBlank()) {
                    callback.invoke(
                        newExtractorLink(
                            source = this.name,
                            name = "AnimeSaturn (Alt)",
                            url = altVideoUrl,
                            type = ExtractorLinkType.VIDEO
                        ) {
                            this.quality = 1080
                            this.referer = mainUrl
                        }
                    )
                    return
                }
            }
            
        } catch (e: Exception) {
        }
    }
}
