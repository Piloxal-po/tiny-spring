package com.github.oxal.spring.servlet;

import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;

import java.time.temporal.Temporal;
import java.util.HashMap;
import java.util.Map;

@Getter
public class TinyResponse<T> {
    private int statut;
    private T data;
    private Map<String, String> headerString;
    private Map<String, Temporal> headerDate;
    private Map<String, Integer> headerInt;
    private Map<String, String> cookies;
    private String encoding;
    private String contentType;

    private TinyResponse(int statut, T data) {
        this.statut = statut;
        this.data = data;
        this.encoding = "UTF-8";
        if (!(data instanceof String)) {
            this.contentType = "application/json";
        } else {
            this.contentType = "text/html";
        }
    }

    public static <T> TinyResponse<T> ok(T data) {
        return new TinyResponse<>(HttpServletResponse.SC_OK, data);
    }

    public static <T> TinyResponse<T> badRequest(T data) {
        return new TinyResponse<>(HttpServletResponse.SC_BAD_REQUEST, data);
    }

    public static <T> TinyResponse<T> forbidden(T data) {
        return new TinyResponse<>(HttpServletResponse.SC_FORBIDDEN, data);
    }

    public static <T> TinyResponse<T> notFound(T data) {
        return new TinyResponse<>(HttpServletResponse.SC_NOT_FOUND, data);
    }

    public static <T> TinyResponse<T> internalServerError(T data) {
        return new TinyResponse<>(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, data);
    }

    public static <T> TinyResponse<T> instance(int statut, T data) {
        return new TinyResponse<>(statut, data);
    }

    public TinyResponse<T> setHeaderString(Map<String, String> headerString) {
        this.headerString = headerString;
        return this;
    }

    public TinyResponse<T> setHeaderString(String headerString, String value) {
        if (this.headerString == null) {
            this.headerString = new HashMap<>();

        }
        this.headerString.put(headerString, value);
        return this;
    }

    public TinyResponse<T> setHeaderDate(Map<String, Temporal> headerDate) {
        this.headerDate = headerDate;
        return this;
    }

    public TinyResponse<T> setHeaderDate(String headerString, Temporal value) {
        if (this.headerDate == null) {
            this.headerDate = new HashMap<>();

        }
        this.headerDate.put(headerString, value);
        return this;
    }

    public TinyResponse<T> setHeaderInt(Map<String, Integer> headerInt) {
        this.headerInt = headerInt;
        return this;
    }

    public TinyResponse<T> setHeaderInt(String headerString, Integer value) {
        if (this.headerInt == null) {
            this.headerInt = new HashMap<>();

        }
        this.headerInt.put(headerString, value);
        return this;
    }

    public TinyResponse<T> setCookies(Map<String, String> cookies) {
        this.cookies = cookies;
        return this;
    }

    public TinyResponse<T> setCookies(String headerString, String value) {
        if (this.cookies == null) {
            this.cookies = new HashMap<>();

        }
        this.cookies.put(headerString, value);
        return this;
    }

    public TinyResponse<T> setEncoding(String encoding) {
        this.encoding = encoding;
        return this;
    }

    public TinyResponse<T> setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }
}
