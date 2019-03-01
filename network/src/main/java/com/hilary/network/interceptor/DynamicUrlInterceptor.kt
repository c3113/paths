package com.hilary.network.interceptor

import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Response

//  Created by administrator on 2019/3/1.
//  Copyright (c) 2019 android-client. All rights reserved.
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
 * 动态设置请求URL
 */
class DynamicUrlInterceptor: Interceptor {

    var host: String = ""

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val newUrl: HttpUrl? = if (host.isBlank()) {null} else {request.url().newBuilder().host(host).build()}
        return if (newUrl == null) chain.proceed(request) else chain.proceed(request.newBuilder().url(newUrl).build())

    }
}