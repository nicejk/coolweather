package com.example.coolwheather.util

import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * @Description: 网络请求工具类
 * @author: haishan
 * @Date: 2020/9/5
 */
class HttpUtil {
    companion object {
        fun sendOkHttpRequest(address: String, callback: okhttp3.Callback) {
            val client = OkHttpClient()
            val request = Request.Builder().url(address).build()
            client.newCall(request).enqueue(callback)
        }
    }
}