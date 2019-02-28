package com.hilary.network.util

import com.hilary.network.NETWORK_SUCCESS_CODE
import retrofit2.Response

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
 * API响应使用的公共类。
 * @param <T>   响应对象的类型
 */
sealed class ApiResponse<R> {
    companion object {
        /**
         * 创建响应失败结果
         */
        fun <T> create(error: Throwable): ApiErrorResponse<T> {
            return ApiErrorResponse(error.message ?: "unknown error")
        }

        /**
         *
         */
        fun <T : BaseResponse<*>> create(response: Response<T>): ApiResponse<T> {
            return if (response.isSuccessful) {
                val body = response.body()
                return when {
                    body == null -> ApiEmptyResponse()
                    body.code == NETWORK_SUCCESS_CODE -> ApiSuccessResponse(body)
                    else -> ApiErrorResponse(body.message)
                }
            } else {
                val msg = response.errorBody()?.string()
                val errorMsg = if (msg.isNullOrEmpty()) {
                    response.message()
                } else {
                    msg
                }
                ApiErrorResponse(errorMsg ?: "unknown error")
            }
        }
    }
}

/**
 * API响应结果为正常
 */
data class ApiSuccessResponse<T>(val body: T): ApiResponse<T>()

/**
 * API响应结果为空
 */
class ApiEmptyResponse<T> : ApiResponse<T>()

/**
 * API响应失败
 */
data class ApiErrorResponse<T>(val errorMessage: String) : ApiResponse<T>()