package com.dionys.mymap.util;

import okhttp3.MediaType;

public class HttpUtil {
    private static final String SERVER_IP = "192.168.137.161";

    private static final String SERVER_PORT = "8000";

    public static final MediaType JASON = MediaType.parse("application/json; charset=utf-8");


    public static String getUrl() {
        return SERVER_IP + ":" + SERVER_PORT;
    }
}
