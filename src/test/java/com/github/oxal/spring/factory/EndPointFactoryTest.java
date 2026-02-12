package com.github.oxal.spring.factory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.oxal.context.Context;
import com.github.oxal.object.KeyDefinition;
import com.github.oxal.resolver.BeanDefinitionResolver;
import com.github.oxal.runner.ApplicationRunner;
import com.github.oxal.spring.enumeration.Endpoint;
import com.github.oxal.spring.enumeration.operator.Get;
import com.github.oxal.spring.enumeration.operator.Post;
import com.github.oxal.spring.enumeration.param.PathParam;
import com.github.oxal.spring.enumeration.param.QueryParam;
import com.github.oxal.spring.enumeration.param.RequestBody;
import com.github.oxal.spring.servlet.TinyResponse;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class EndPointFactoryTest {

    @Mock
    private Context context;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private MockedStatic<BeanDefinitionResolver> beanDefinitionResolverMock;
    private MockedStatic<ApplicationRunner> applicationRunnerMock;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();

        beanDefinitionResolverMock = mockStatic(BeanDefinitionResolver.class);
        applicationRunnerMock = mockStatic(ApplicationRunner.class);

        // Mock request attributes behavior
        final Map<String, Object> attributes = new HashMap<>();
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

    @AfterEach
    void tearDown() {
        beanDefinitionResolverMock.close();
        applicationRunnerMock.close();
    }

    @Test
    void testBuildEndpointWithGetMethod() throws Exception {
        // Setup
        beanDefinitionResolverMock.when(() -> BeanDefinitionResolver.resolve(any(), any(), any())).thenReturn(KeyDefinition.builder().build());
        applicationRunnerMock.when(() -> ApplicationRunner.loadBean(TestController.class)).thenReturn(new TestController());
        applicationRunnerMock.when(() -> ApplicationRunner.loadBean(ObjectMapper.class)).thenReturn(objectMapper);

        HttpServlet servlet = EndPointFactory.buildEndpoint(TestController.class, context);
        assertNotNull(servlet);

        // Simulate Request
        when(request.getMethod()).thenReturn("GET");
        when(request.getPathInfo()).thenReturn("/test");
        StringWriter stringWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        // Invoke
        java.lang.reflect.Method doGet = servlet.getClass().getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGet.setAccessible(true);
        doGet.invoke(servlet, request, response);

        // Verify
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertEquals("Hello", stringWriter.toString());
    }

    @Test
    void testBuildEndpointWithPathParam() throws Exception {
        // Setup
        beanDefinitionResolverMock.when(() -> BeanDefinitionResolver.resolve(any(), any(), any())).thenReturn(KeyDefinition.builder().build());
        applicationRunnerMock.when(() -> ApplicationRunner.loadBean(TestController.class)).thenReturn(new TestController());
        applicationRunnerMock.when(() -> ApplicationRunner.loadBean(ObjectMapper.class)).thenReturn(objectMapper);

        HttpServlet servlet = EndPointFactory.buildEndpoint(TestController.class, context);

        // Simulate Request
        when(request.getMethod()).thenReturn("GET");
        when(request.getPathInfo()).thenReturn("/test/123");
        StringWriter stringWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        // Invoke
        java.lang.reflect.Method doGet = servlet.getClass().getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGet.setAccessible(true);
        doGet.invoke(servlet, request, response);

        // Verify
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertEquals("ID: 123", stringWriter.toString());
    }

    @Test
    void testBuildEndpointWithQueryParam() throws Exception {
        // Setup
        beanDefinitionResolverMock.when(() -> BeanDefinitionResolver.resolve(any(), any(), any())).thenReturn(KeyDefinition.builder().build());
        applicationRunnerMock.when(() -> ApplicationRunner.loadBean(TestController.class)).thenReturn(new TestController());
        applicationRunnerMock.when(() -> ApplicationRunner.loadBean(ObjectMapper.class)).thenReturn(objectMapper);

        HttpServlet servlet = EndPointFactory.buildEndpoint(TestController.class, context);

        // Simulate Request
        when(request.getMethod()).thenReturn("GET");
        when(request.getPathInfo()).thenReturn("/query");
        when(request.getParameter("q")).thenReturn("search");
        StringWriter stringWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        // Invoke
        java.lang.reflect.Method doGet = servlet.getClass().getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGet.setAccessible(true);
        doGet.invoke(servlet, request, response);

        // Verify
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertEquals("Query: search", stringWriter.toString());
    }

    @Test
    void testBuildEndpointWithRequestBody() throws Exception {
        // Setup
        beanDefinitionResolverMock.when(() -> BeanDefinitionResolver.resolve(any(), any(), any())).thenReturn(KeyDefinition.builder().build());
        applicationRunnerMock.when(() -> ApplicationRunner.loadBean(TestController.class)).thenReturn(new TestController());
        applicationRunnerMock.when(() -> ApplicationRunner.loadBean(ObjectMapper.class)).thenReturn(objectMapper);

        HttpServlet servlet = EndPointFactory.buildEndpoint(TestController.class, context);

        // Simulate Request
        when(request.getMethod()).thenReturn("POST");
        when(request.getPathInfo()).thenReturn("/post");
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader("{\"name\":\"test\"}")));
        StringWriter stringWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        // Invoke
        java.lang.reflect.Method doPost = servlet.getClass().getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, request, response);

        // Verify
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertEquals("Posted: test", stringWriter.toString());
    }

    @Test
    void testBuildEndpointInvalidClass() {
        beanDefinitionResolverMock.when(() -> BeanDefinitionResolver.resolve(any(), any(), any())).thenReturn(null);
        assertThrows(RuntimeException.class, () -> EndPointFactory.buildEndpoint(TestController.class, context));
    }

    // --- Test Controller ---
    @Endpoint
    static class TestController {

        @Get("/test")
        public TinyResponse<String> hello() {
            return TinyResponse.ok("Hello");
        }

        @Get("/test/{id}")
        public TinyResponse<String> withId(@PathParam("id") int id) {
            return TinyResponse.ok("ID: " + id);
        }

        @Get("/query")
        public TinyResponse<String> withQuery(@QueryParam("q") String q) {
            return TinyResponse.ok("Query: " + q);
        }

        @Post("/post")
        public TinyResponse<String> withBody(@RequestBody TestBody body) {
            return TinyResponse.ok("Posted: " + body.name);
        }
    }

    static class TestBody {
        public String name;
    }
}
