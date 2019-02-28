package com.hilary.network

import com.facebook.stetho.okhttp3.StethoInterceptor
import com.hilary.network.interceptor.HttpLoggingInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

//  Created by administrator on 2019/2/28.
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
class NetworkPortal {
    /**
     * retrofit实例
     */
    private lateinit var retrofit: Retrofit

    init {
        val builder = OkHttpClient.Builder()
        // 设置网络请求超时时间
        builder.readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
        builder.writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
        builder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
        // 失败后尝试重新请求
        builder.retryOnConnectionFailure(RETRY_CONNECTION_FAILURE)

        if (BuildConfig.DEBUG) {
            //添加Log打印
            builder.addNetworkInterceptor(HttpLoggingInterceptor())
            //添加stetHo 网络检测
            builder.addNetworkInterceptor(StethoInterceptor())
        }
        //构建Retrofit
        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(builder.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    }
}