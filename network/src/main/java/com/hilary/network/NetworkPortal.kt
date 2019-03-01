package com.hilary.network

import com.facebook.stetho.okhttp3.StethoInterceptor
import com.hilary.network.NetworkPortal.retrofit
import com.hilary.network.NetworkPortal.serviceMap
import com.hilary.network.interceptor.DynamicUrlInterceptor
import com.hilary.network.interceptor.HeaderParamsInterceptor
import com.hilary.network.interceptor.HttpLoggingInterceptor
import com.hilary.network.util.LiveDataCallAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
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
object NetworkPortal {
    /**
     * retrofit实例
     */
    internal var retrofit: Retrofit
    internal val dynamicUrlInterceptor = DynamicUrlInterceptor()
    internal val serviceMap = TreeMap<String, Any>()

    init {
        val builder = OkHttpClient.Builder()
        // 设置网络请求超时时间
        builder.readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
        builder.writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
        builder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
        // 失败后尝试重新请求
        builder.retryOnConnectionFailure(RETRY_CONNECTION_FAILURE)
        //添加Header
        builder.addInterceptor(HeaderParamsInterceptor())
        //添加动态Url功能
        builder.addInterceptor(dynamicUrlInterceptor)

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
            .addCallAdapterFactory(LiveDataCallAdapterFactory())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    }

}

/**
 * 获取默认Service对象
 */
@Suppress("unused")
fun <T> getService(clazz: Class<T>): T {
    return getService(clazz, "")
}

/**
 * 获取动态url Service对象
 */
fun <T> getService(clazz: Class<T>, url: String): T {
    NetworkPortal.dynamicUrlInterceptor.host = url
    return when {
        url.isNotBlank() -> NetworkPortal.retrofit.create(clazz)
        serviceMap.containsKey(clazz.simpleName) -> serviceMap[clazz.simpleName] as T
        else -> {
            val service = retrofit.create(clazz)
            serviceMap[clazz.simpleName] = serviceMap
            service
        }
    }
}