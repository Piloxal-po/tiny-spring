package com.github.oxal.spring.utils;

public class UrlUtils {
    public static String buildUrl(String baseUrl, String url) {
        if (baseUrl == null || baseUrl.isEmpty()) {
            return buildUrl(url);
        }
        if (!baseUrl.startsWith("/")) {
            baseUrl = "/" + baseUrl;
        }
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        if (url.startsWith("/")) {
            url = url.substring(1);
        }
        return  String.format("%s/%s", baseUrl, url);
    }

    public static String buildUrl(String url) {
        if (!url.startsWith("/")) {
            url = "/" + url;
        }
        return url;
    }
}
