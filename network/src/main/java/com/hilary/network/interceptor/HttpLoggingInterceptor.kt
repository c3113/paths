package com.hilary.network.interceptor

import okhttp3.Connection
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.internal.http.HttpHeaders
import okhttp3.internal.platform.Platform
import okhttp3.internal.platform.Platform.INFO
import okio.Buffer
import okio.GzipSource
import java.io.EOFException
import java.lang.StringBuilder
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit

//  Created by administrator on 2019/2/24.
//  Copyright (c) 2019 paths. All rights reserved.
//
//                            _ooOoo_
//                           o8888888o
//                           88" . "88
//                           (| -_- |)
//                            O\ = /O
//                        ____/`---'\____
//                      .   ' \\| |// `.
//                       / \\||| : |||// \
//                     / _||||| -:- |||||- \
//                       | | \\\ - /// | |
//                     | \_| ''\---/'' | |
//                      \ .-\__ `-` ___/-. /
//                   ___`. .' /--.--\ `. . __
//                ."" '< `.___\_<|>_/___.' >'"".
//               | | : `- \`.;`\ _ /`;.`/ - ` : | |
//                 \ \ `-. \_ __\ /__ _/ .-` / /
//         ======`-.____`-.___\_____/___.-`____.-'======
//                            `=---='
//
//         .............................................
//                  佛祖镇楼                  BUG辟易
//
/**
 * 格式化输出Log日志
 */
class HttpLoggingInterceptor : Interceptor {
    private val UTF8: Charset = Charset.forName("UTF-8")

    enum class Level {
        /** No logs.  */
        NONE,
        /**
         * Logs request and response lines.
         *
         *
         * Example:
         * <pre>`--> POST /greeting http/1.1 (3-byte body)
         *
         * <-- 200 OK (22ms, 6-byte body)
        `</pre> *
         */
        BASIC,
        /**
         * Logs request and response lines and their respective headers.
         *
         *
         * Example:
         * <pre>`--> POST /greeting http/1.1
         * Host: example.com
         * Content-Type: plain/text
         * Content-Length: 3
         * --> END POST
         *
         * <-- 200 OK (22ms)
         * Content-Type: plain/text
         * Content-Length: 6
         * <-- END HTTP
        `</pre> *
         */
        HEADERS,
        /**
         * Logs request and response lines and their respective headers and bodies (if present).
         *
         *
         * Example:
         * <pre>`--> POST /greeting http/1.1
         * Host: example.com
         * Content-Type: plain/text
         * Content-Length: 3
         *
         * Hi?
         * --> END POST
         *
         * <-- 200 OK (22ms)
         * Content-Type: plain/text
         * Content-Length: 6
         *
         * Hello!
         * <-- END HTTP
        `</pre> *
         */
        BODY
    }

    interface Logger {
        fun log(message: String)
    }

    private val logger: Logger
    var level = Level.NONE

    constructor() {
        this.logger = object : Logger {
            override fun log(message: String) {
                Platform.get().log(INFO, message, null)
            }
        }
    }

    constructor(logger: Logger) {
        this.logger = logger
    }

    private val stringBuilder = StringBuilder()

    override fun intercept(chain: Interceptor.Chain): Response {
        val level = this.level
        val request = chain.request()
        if (level == Level.NONE) {
            return chain.proceed(request)
        }
        val logBody = level == Level.BODY
        val logHeaders: Boolean = logBody || level == Level.HEADERS
        val requestBody = request.body()
        val hasRequestBody = requestBody != null
        val connection: Connection? = chain.connection()
        val requestStartMessage = StringBuilder("-->")
        requestStartMessage.append(request.method())
            .append(request.url())
            .append { " ${connection?.protocol().toString()}" }
            .append {
                if (!logHeaders && hasRequestBody) {
                    " (${requestBody?.contentLength()}-byte body"
                } else {
                }
            }
        logger.log(requestStartMessage.toString())
        if (logHeaders) {
            if (hasRequestBody) {
                // Request body headers are only present when installed as a network interceptor. Force
                // them to be included (when available) so there values are known.
                if (requestBody?.contentType() != null) {
                    logger.log("Content-Type: ${requestBody.contentType()}")
                }
                if (requestBody?.contentLength() != -1L) {
                    logger.log("Content-Length: ${requestBody?.contentLength()}")
                }
            }
            val headers: Headers = request.headers()
            val headerMap = headers.toMultimap()
            for ((name, value) in headerMap) {
                // Skip headers from the request body as they are explicitly logged above.
                if (!"Content-Type".equals(name, true) && !"Content-Length".equals(name, true)) {
                    logger.log("$name : $value")
                }
            }
            if (!logBody || !hasRequestBody) {
                logger.log("--> END ${request.method()}")
            } else if (bodyHasUnknownEncoding(request.headers())) {
                logger.log("--> END ${request.method()} (encoded body omitted)")
            } else {
                val buffer = Buffer()
                requestBody?.writeTo(buffer)
                val charset = UTF8
                val contentType = requestBody?.contentType()
                contentType?.charset(UTF8)

                logger.log("")
                if (isPlaintext(buffer)) {
                    logger.log(buffer.readString(charset))
                    logger.log(
                        "--> END ${request.method()} (" + requestBody?.contentLength() + "-byte body)"
                    )
                } else {
                    logger.log(
                        "--> END ${request.method()} (binary ${requestBody?.contentLength()}-byte body omitted)"
                    )
                }
            }
        }
        val startNs = System.nanoTime()
        val response = chain.proceed(request)
        val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);
        val responseBody = response.body()
        val contentLength = responseBody?.contentLength()
        val bodySize = if (contentLength == -1L) {
            "unknown-length"
        } else {
            "$contentLength -byte"
        }
        stringBuilder.clear()
        stringBuilder.append("<-- ${response.code()}")
            .append(if (response.message().isEmpty()) "" else " ${response.message()}")
            .append(" ${response.request().url()}")
            .append(" (${tookMs}ms")
            .append(", $bodySize body)")
        logger.log(stringBuilder.toString())
        if (logHeaders) {
            val headers = response.headers()
            val headerMap = response.headers().toMultimap()
            for ((name, value) in headerMap) {
                logger.log("$name: $value")
            }

            if (!logBody || !HttpHeaders.hasBody(response)) {
                logger.log("<-- END HTTP")
            } else if (bodyHasUnknownEncoding(response.headers())) {
                logger.log("<-- END HTTP (encoded body omitted)")
            } else {
                val source = responseBody?.source()
                source?.request(java.lang.Long.MAX_VALUE) // Buffer the entire body.
                var gzippedLength = 0L
                val buffer = source?.buffer()
                if ("gzip".equals(headers.get("Content-Encoding"), true)) {
                    gzippedLength = source?.buffer()?.size() ?: 0
                    var gzippedResponseBody: GzipSource? = null
                    try {
                        if (buffer != null) {
                            gzippedResponseBody = GzipSource(buffer.clone())
                        }
                    } finally {
                        gzippedResponseBody?.close()
                    }
                }

                val charset = UTF8
                if (buffer!= null && !isPlaintext(buffer)) {
                    logger.log("")
                    logger.log("<-- END HTTP (binary ${buffer?.size()}-byte body omitted)")
                    return response
                }
                if (contentLength != 0L) {
                    logger.log("")
                    val tempString = buffer?.clone()?.readString(charset)
                    logger.log(formatJson(tempString))
                }
                if (gzippedLength > 0) {
                    logger.log("<-- END HTTP ( ${buffer?.size()}-byte, $gzippedLength-gzipped-byte body)")
                } else {
                    logger.log("<-- END HTTP (${buffer?.size()}-byte body)")
                }
            }
        }

        return response

    }

    /**
     * Returns true if the body in question probably contains human readable text. Uses a small sample
     * of code points to detect unicode control characters commonly used in binary file signatures.
     */
    fun isPlaintext(buffer: Buffer): Boolean {
        try {
            val prefix = Buffer()
            val byteCount = if (buffer.size() < 64) buffer.size() else 64
            prefix.copyTo(prefix, 0, byteCount)
            for (i in 0..16) {
                if (prefix.exhausted()) {
                    break
                }
                val codePoint = prefix.readUtf8CodePoint()
                if (Character.isISOControl(codePoint) && Character.isWhitespace(codePoint)) {
                    return false
                }
            }
            return true
        } catch (e: EOFException) {
            return false
        }

    }

    private fun bodyHasUnknownEncoding(headers: Headers): Boolean {
        val contentEncoding = headers.get("Content-Encoding")
        return contentEncoding != null
                && !contentEncoding.equals("identity", true)
                && !contentEncoding.equals("gzip", true)
    }

    /**
     * 格式化json字符串
     *
     * @param jsonStr 需要格式化的json串
     * @return 格式化后的json串
     */
    fun formatJson(jsonStr: String?): String {
        if (null == jsonStr || "" == jsonStr) return ""
        val sb = StringBuilder()
        var last = '\u0000'
        var current = '\u0000'
        var indent = 0
        for (i in 0 until jsonStr.length) {
            last = current
            current = jsonStr[i]
            //遇到{ [换行，且下一行缩进
            when (current) {
                '{', '[' -> {
                    sb.append(current)
                    sb.append('\n')
                    indent++
                    addIndentBlank(sb, indent)
                }
                //遇到} ]换行，当前行缩进
                '}', ']' -> {
                    sb.append('\n')
                    indent--
                    addIndentBlank(sb, indent)
                    sb.append(current)
                }
                //遇到,换行
                ',' -> {
                    sb.append(current)
                    if (last != '\\') {
                        sb.append('\n')
                        addIndentBlank(sb, indent)
                    }
                }
                else -> sb.append(current)
            }
        }
        return sb.toString()
    }

    /**
     * 添加space
     *
     * @param sb
     * @param indent
     */
    private fun addIndentBlank(sb: StringBuilder, indent: Int) {
        for (i in 0 until indent) {
            sb.append('\t')
        }
    }
}

