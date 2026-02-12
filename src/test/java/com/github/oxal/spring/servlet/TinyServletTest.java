package com.github.oxal.spring.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class TinyServletTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private TinyServlet tinyServlet;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        tinyServlet = TinyServlet.builder()
                .objectMapper(objectMapper)
                .build();

        // Mock request attributes behavior
        final java.util.Map<String, Object> attributes = new java.util.HashMap<>();
        doAnswer(invocation -> {
            String key = invocation.getArgument(0);
            Object value = invocation.getArgument(1);
            attributes.put(key, value);
            return null;
        }).when(request).setAttribute(anyString(), any());

        when(request.getAttribute(anyString())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0);
            return attributes.get(key);
        });
    }

    @Test
    void testRouteDefinitionFromPath() {
        TinyServlet.RouteDefinition routeDef = TinyServlet.RouteDefinition.fromPath("/users/{id}");
        assertNotNull(routeDef);
        assertEquals(1, routeDef.paramNames().size());
        assertEquals("id", routeDef.paramNames().get(0));
        assertTrue(routeDef.pattern().matcher("/users/123").matches());
        assertFalse(routeDef.pattern().matcher("/users/123/details").matches());
    }

    @Test
    void testHandleRequestGetRouteFound() throws ServletException, IOException {
        // Setup route
        TinyServlet.RouteDefinition routeDef = TinyServlet.RouteDefinition.fromPath("/hello");
        Function<HttpServletRequest, TinyResponse<?>> handler = req -> TinyResponse.ok("Hello World");
        TinyServlet.Route route = new TinyServlet.Route(routeDef, handler);
        tinyServlet.setGetRoutes(List.of(route));

        // Setup request
        when(request.getPathInfo()).thenReturn("/hello");
        when(request.getMethod()).thenReturn("GET");
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        // Execute
        HttpServlet servlet = tinyServlet.createServlet();
        try {
            java.lang.reflect.Method doGet = servlet.getClass().getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
            doGet.setAccessible(true);
            doGet.invoke(servlet, request, response);
        } catch (Exception e) {
            fail("Failed to invoke doGet: " + e.getMessage());
        }

        // Verify
        verify(response).setStatus(HttpServletResponse.SC_OK);
        verify(response).setContentType("text/html"); // Default for String data
        writer.flush();
        assertEquals("Hello World", stringWriter.toString());
    }

    @Test
    void testHandleRequestRouteNotFound() throws ServletException, IOException {
        // Setup empty routes
        tinyServlet.setGetRoutes(Collections.emptyList());

        // Setup request
        when(request.getPathInfo()).thenReturn("/unknown");
        when(request.getMethod()).thenReturn("GET");

        // Execute
        HttpServlet servlet = tinyServlet.createServlet();
        try {
            java.lang.reflect.Method doGet = servlet.getClass().getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
            doGet.setAccessible(true);
            doGet.invoke(servlet, request, response);
        } catch (Exception e) {
            fail("Failed to invoke doGet: " + e.getMessage());
        }

        // Verify
        verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    void testHandleRequestWithParams() throws ServletException, IOException {
        // Setup route with params
        TinyServlet.RouteDefinition routeDef = TinyServlet.RouteDefinition.fromPath("/users/{id}");
        Function<HttpServletRequest, TinyResponse<?>> handler = req -> {
            @SuppressWarnings("unchecked")
            java.util.Map<String, String> params = (java.util.Map<String, String>) req.getAttribute("pathParams");
            return TinyResponse.ok("User " + params.get("id"));
        };
        TinyServlet.Route route = new TinyServlet.Route(routeDef, handler);
        tinyServlet.setGetRoutes(List.of(route));

        // Setup request
        when(request.getPathInfo()).thenReturn("/users/42");
        when(request.getMethod()).thenReturn("GET");
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        // Execute
        HttpServlet servlet = tinyServlet.createServlet();
        try {
            java.lang.reflect.Method doGet = servlet.getClass().getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
            doGet.setAccessible(true);
            doGet.invoke(servlet, request, response);
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            fail("Failed to invoke doGet: " + cause.toString());
        }

        // Verify
        verify(request).setAttribute(eq("pathParams"), any());
        verify(response).setStatus(HttpServletResponse.SC_OK);
        writer.flush();
        assertEquals("User 42", stringWriter.toString());
    }

    @Test
    void testJsonResponse() throws ServletException, IOException {
        // Setup route returning object
        TinyServlet.RouteDefinition routeDef = TinyServlet.RouteDefinition.fromPath("/api/data");
        TestDto dto = new TestDto("test", 123);
        Function<HttpServletRequest, TinyResponse<?>> handler = req -> TinyResponse.ok(dto);
        TinyServlet.Route route = new TinyServlet.Route(routeDef, handler);
        tinyServlet.setGetRoutes(List.of(route));

        // Setup request
        when(request.getPathInfo()).thenReturn("/api/data");
        when(request.getMethod()).thenReturn("GET");
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        // Execute
        HttpServlet servlet = tinyServlet.createServlet();
        try {
            java.lang.reflect.Method doGet = servlet.getClass().getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
            doGet.setAccessible(true);
            doGet.invoke(servlet, request, response);
        } catch (Exception e) {
            fail("Failed to invoke doGet: " + e.getMessage());
        }

        // Verify
        verify(response).setStatus(HttpServletResponse.SC_OK);
        verify(response).setContentType("application/json");
        writer.flush();
        assertEquals(objectMapper.writeValueAsString(dto), stringWriter.toString());
    }

    // Helper DTO for JSON test
    static class TestDto {
        public String name;
        public int value;

        public TestDto(String name, int value) {
            this.name = name;
            this.value = value;
        }
    }
}
