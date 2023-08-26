package com.stor.storyofegupt.network

import android.util.Log
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class NetworkRequest {
    fun getContent(urlString: String, deep: String?, referer: String): String? {
        Log.i("Get content", "Url: ${urlString.format(deep, referer)}")
        val url = URL(urlString.format(deep, referer))
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            readTimeout = 10000
        }
        connection.connect()
        return workWithStreams(connection.inputStream)
    }

    private fun workWithStreams(inputStream: InputStream): String {
        val reader = BufferedReader(InputStreamReader(inputStream))
        return reader.readLines().reduce { acc, s -> "$acc\n$s" }
    }

    companion object {
        const val URL_FOR_TEST = "https://sampiset.com/lauth/?deep=%s&%s"
    }
}