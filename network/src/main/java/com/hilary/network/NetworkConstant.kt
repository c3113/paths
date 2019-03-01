package com.hilary.network

import android.content.Context

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

/**
 * 网络请求超时时间值(s)
 */
const val DEFAULT_TIMEOUT = 30L
/**
 * 请求默认地址
 */
const val BASE_URL = ""
/**
 * 失败后尝试重新请求
 */
const val RETRY_CONNECTION_FAILURE = true


/*###########################网络请求Code#Start##########################################*/
/**
 * 网络请求成功
 */
const val NETWORK_SUCCESS_CODE = 200
/**
 * 网络异常
 */
const val NETWORK_ERROR_CODE = 404
/**
 * Token失效
 */
const val NETWORK_ERROR_TOKEN_CODE = 500

/*###########################网络请求Code#End##########################################*/

/*###########################网络请求Message#Start##########################################*/

var NETWORK_ERROR_MESSAGE = "网络连接错误，请检查网络连接"
var NETWORK_ERROR_TIME_OUT_MESSAGE = "请求超时，请重试"
var NETWORK_ERROR_DATA_MESSAGE = "数据异常，请重试"
var NETWORK_ERROR_REQUEST_MESSAGE = "无效的请求"
var NETWORK_ERROR_REQUEST_INTERCEPT_MESSAGE = "操作过于频繁，请稍后重试"

fun initErrorMessage(context: Context) {
    NETWORK_ERROR_MESSAGE = context.getString(R.string.error_network)
    NETWORK_ERROR_TIME_OUT_MESSAGE = context.getString(R.string.error_time_out)
    NETWORK_ERROR_DATA_MESSAGE = context.getString(R.string.error_json)
    NETWORK_ERROR_REQUEST_MESSAGE = context.getString(R.string.error_request)
    NETWORK_ERROR_REQUEST_INTERCEPT_MESSAGE = context.getString(R.string.error_request_intercept)
}

/*###########################网络请求Message#End##########################################*/