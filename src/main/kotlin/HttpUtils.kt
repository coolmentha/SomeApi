package org.example.mirai.plugin

import net.mamoe.mirai.utils.MiraiLogger
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.ByteArrayOutputStream


import java.io.IOException

import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
val logger = MiraiLogger.Factory.create(HttpUtils::class)
class HttpUtils {
    companion object{
        private val client = OkHttpClient()
        suspend fun downloadBytes(url: String): ByteArray? = suspendCoroutine { continuation ->
            val request = Request.Builder()
                .url(url)
                .build()

            client.newCall(request).enqueue(object : okhttp3.Callback {
                override fun onFailure(call: okhttp3.Call, e: IOException) {
                    logger.error("Exception occurred: ${e.message}")
                    continuation.resume(null)
                }

                override fun onResponse(call: okhttp3.Call, response: Response) {
                    if (response.isSuccessful) {
                        response.body?.byteStream()?.use { inputStream ->
                            ByteArrayOutputStream().use { outputStream ->
                                val buffer = ByteArray(4096)
                                var bytesRead: Int
                                while (inputStream.read(buffer).also { bytesRead = it }!= -1) {
                                    outputStream.write(buffer, 0, bytesRead)
                                }
                                continuation.resume(outputStream.toByteArray())
                            }
                        }
                    } else {
                        logger.error("Failed to download, response code: ${response.code}")
                        continuation.resume(null)
                    }
                }
            })
        }

        fun closeClient() {
            client.dispatcher.executorService.shutdown()
            client.connectionPool.evictAll()
        }

        suspend fun execute(url:String):String? = suspendCoroutine { continuation->
            val request = Request.Builder()
                .url(url)
                .build()
            client.newCall(request).enqueue(object : okhttp3.Callback {
                override fun onFailure(call: okhttp3.Call, e: IOException) {
                    logger.error("Exception occurred: ${e.message}")
                    continuation.resume(null)
                }

                override fun onResponse(call: okhttp3.Call, response: Response) {
                    if (response.isSuccessful) {
                        continuation.resume(response.body?.string())
                    } else {
                        logger.error("Failed to download, response code: ${response.code}")
                        continuation.resume(null)
                    }
                }
            })
        }
    }
}