package com.hilary.network.interceptor

import com.hilary.network.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response

//  Created by administrator on 2019/3/1.
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
 * 添加固定请求Header
 *
 * addHeaderParams是否自header请求，默认是开启
 * 请求第三方接口使用过滤掉自己平台特定参数
 *
 * 动态参数需要自己动手
 */
class HeaderParamsInterceptor: Interceptor {

    var addHeaderParams = true

    override fun intercept(chain: Interceptor.Chain): Response {
        return if (addHeaderParams) {
            chain.proceed(chain.request())
        } else {
            val request = chain.request().newBuilder()
                .addHeader("VERSION_NAME", BuildConfig.VERSION_NAME)
                .addHeader("VERSION_CODE", BuildConfig.VERSION_CODE.toString())
                .build()
            chain.proceed(request)
        }
    }
}