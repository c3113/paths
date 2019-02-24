package com.hilary.network

import okhttp3.Connection
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.internal.platform.Platform
import okhttp3.internal.platform.Platform.INFO
import okio.Buffer
import java.lang.NullPointerException
import java.lang.StringBuilder
import java.nio.charset.Charset
import kotlin.math.log

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
class HttpLoggingInterceptor: Interceptor {
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

    val logger: Logger
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
            .append{" ${connection?.protocol().toString()}"}
            .append{if (!logHeaders && hasRequestBody){" (${requestBody?.contentLength()}-byte body"} else {}}
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
        }
    }

    /**
     * Returns true if the body in question probably contains human readable text. Uses a small sample
     * of code points to detect unicode control characters commonly used in binary file signatures.
     */
    fun isPlaintext(buffer: Buffer): Boolean {
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
    }

    private fun bodyHasUnknownEncoding(headers: Headers): Boolean {
        val contentEncoding = headers.get("Content-Encoding")
        return contentEncoding != null
                && !contentEncoding.equals("identity", true)
                && !contentEncoding.equals("gzip", true)
    }
}

