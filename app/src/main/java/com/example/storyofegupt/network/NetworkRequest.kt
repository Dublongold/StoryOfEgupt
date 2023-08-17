package com.example.storyofegupt.network

import android.content.Context
import android.util.Log
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.ByteBuffer
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class NetworkRequest(private val context: Context) {
    fun getContent(urlString: String): String? {
        val url: URL = URL(urlString)
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            readTimeout = 10000
        }
        connection.connect()
        return workWithStreams(connection.inputStream)
    }

    private fun workWithStreams(inputStream: InputStream): String? {
        val reader = BufferedReader(InputStreamReader(inputStream))
        return reader.readLines().reduce { acc, s -> "$acc\n$s" }
    }

    companion object {
        const val URL_FOR_TEST = "https://gist.githubusercontent.com/lubetel/b0399e72219b149222ab4dbd5105f7c0/raw/025beb00e63d8383504a0b8d97e69310b99e4eb1/gistfile1.txt"
    }
}