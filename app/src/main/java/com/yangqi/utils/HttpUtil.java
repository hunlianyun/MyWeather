package com.yangqi.utils;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * 网络请求工具类
 */

public class HttpUtil {
    public static void sendOkHttpRequest(String address, Callback callback) {

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }
}
