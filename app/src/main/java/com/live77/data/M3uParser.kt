package com.live77.data

import com.live77.model.Channel
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Fetches and parses an M3U/M3U8 playlist from a remote URL.
 *
 * Expected format:
 *   #EXTM3U
 *   #EXTINF:-1 tvg-logo="..." group-title="Group",Channel Name
 *   http://stream.url/stream.m3u8
 */
object M3uParser {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Downloads and parses the M3U playlist from [url].
     * @throws IOException on network or HTTP errors.
     */
    @Throws(IOException::class)
    fun fetchAndParse(url: String): List<Channel> {
        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw IOException("HTTP ${response.code}: ${response.message}")
        }
        val body = response.body?.string() ?: throw IOException("Empty response body")
        return parse(body)
    }

    /** Parses M3U text content into a list of [Channel] objects. */
    fun parse(content: String): List<Channel> {
        val channels = mutableListOf<Channel>()
        val lines = content.lines()
        var i = 0
        while (i < lines.size) {
            val line = lines[i].trim()
            if (line.startsWith("#EXTINF:")) {
                val name = extractName(line)
                val logo = extractAttribute(line, "tvg-logo")
                val group = extractAttribute(line, "group-title")

                // Find the next non-empty, non-comment line as the stream URL
                var j = i + 1
                while (j < lines.size && (lines[j].isBlank() || lines[j].trim().startsWith("#"))) {
                    j++
                }
                if (j < lines.size) {
                    val streamUrl = lines[j].trim()
                    if (streamUrl.startsWith("http://") || streamUrl.startsWith("https://")) {
                        channels.add(
                            Channel(
                                name = name,
                                url = streamUrl,
                                logoUrl = logo?.takeIf { it.isNotBlank() },
                                group = group?.takeIf { it.isNotBlank() }
                            )
                        )
                    }
                    i = j + 1
                    continue
                }
            }
            i++
        }
        return channels
    }

    private fun extractName(extinf: String): String {
        val commaIdx = extinf.lastIndexOf(',')
        return if (commaIdx >= 0) extinf.substring(commaIdx + 1).trim() else "Unknown"
    }

    private fun extractAttribute(extinf: String, attr: String): String? {
        val regex = Regex("$attr=\"([^\"]*)\"")
        return regex.find(extinf)?.groupValues?.getOrNull(1)
    }
}
