package com.github.oxal.spring.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UrlUtilsTest {

    @Test
    public void testBuildUrlWithBaseUrl() {
        // Cas normal
        assertEquals("/api/users", UrlUtils.buildUrl("/api", "users"));
        assertEquals("/api/users", UrlUtils.buildUrl("/api/", "users"));
        assertEquals("/api/users", UrlUtils.buildUrl("/api", "/users"));
        assertEquals("/api/users", UrlUtils.buildUrl("/api/", "/users"));

        // Cas avec baseUrl sans slash au début
        assertEquals("/api/users", UrlUtils.buildUrl("api", "users"));
        assertEquals("/api/users", UrlUtils.buildUrl("api/", "users"));
        assertEquals("/api/users", UrlUtils.buildUrl("api", "/users"));
        assertEquals("/api/users", UrlUtils.buildUrl("api/", "/users"));
    }

    @Test
    public void testBuildUrlWithoutBaseUrl() {
        // Cas avec baseUrl null ou vide
        assertEquals("/users", UrlUtils.buildUrl(null, "users"));
        assertEquals("/users", UrlUtils.buildUrl("", "users"));
        assertEquals("/users", UrlUtils.buildUrl(null, "/users"));
        assertEquals("/users", UrlUtils.buildUrl("", "/users"));
    }

    @Test
    public void testBuildUrlSingleArgument() {
        // Cas normal
        assertEquals("/users", UrlUtils.buildUrl("users"));
        assertEquals("/users", UrlUtils.buildUrl("/users"));
    }
}
