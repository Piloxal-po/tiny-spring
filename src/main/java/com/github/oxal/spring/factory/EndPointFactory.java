package com.github.oxal.spring.factory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.oxal.context.Context;
import com.github.oxal.resolver.BeanDefinitionResolver;
import com.github.oxal.runner.ApplicationRunner;
import com.github.oxal.spring.enumeration.Endpoint;
import com.github.oxal.spring.enumeration.operator.Delete;
import com.github.oxal.spring.enumeration.operator.Get;
import com.github.oxal.spring.enumeration.operator.Post;
import com.github.oxal.spring.enumeration.operator.Put;
import com.github.oxal.spring.enumeration.param.PathParam;
import com.github.oxal.spring.enumeration.param.QueryParam;
import com.github.oxal.spring.enumeration.param.RequestBody;
import com.github.oxal.spring.servlet.TinyResponse;
import com.github.oxal.spring.servlet.TinyServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class EndPointFactory {

    public static HttpServlet buildEndpoint(Class<?> clazz, Context context) {
        if (BeanDefinitionResolver.resolve(clazz, null, context) == null || !clazz.isAnnotationPresent(Endpoint.class)) {
            throw new RuntimeException("No endpoint found for class " + clazz.getName());
        }

        Object controllerInstance = ApplicationRunner.loadBean(clazz);
        ObjectMapper objectMapper = ApplicationRunner.loadBean(ObjectMapper.class);
        TinyServlet.TinyServletBuilder builder = TinyServlet.builder().objectMapper(objectMapper);

        List<TinyServlet.Route> getRoutes = new ArrayList<>();
        List<TinyServlet.Route> postRoutes = new ArrayList<>();
        List<TinyServlet.Route> putRoutes = new ArrayList<>();
        List<TinyServlet.Route> deleteRoutes = new ArrayList<>();

        for (Method method : controllerInstance.getClass().getMethods()) {
            Function<HttpServletRequest, TinyResponse<?>> handler = createHandler(controllerInstance, method, objectMapper);
            if (method.isAnnotationPresent(Get.class)) {
                Get annotation = method.getAnnotation(Get.class);
                TinyServlet.RouteDefinition routeDef = TinyServlet.RouteDefinition.fromPath(annotation.value());
                getRoutes.add(new TinyServlet.Route(routeDef, handler));
            } else if (method.isAnnotationPresent(Post.class)) {
                Post annotation = method.getAnnotation(Post.class);
                TinyServlet.RouteDefinition routeDef = TinyServlet.RouteDefinition.fromPath(annotation.value());
                postRoutes.add(new TinyServlet.Route(routeDef, handler));
            } else if (method.isAnnotationPresent(Put.class)) {
                Put annotation = method.getAnnotation(Put.class);
                TinyServlet.RouteDefinition routeDef = TinyServlet.RouteDefinition.fromPath(annotation.value());
                putRoutes.add(new TinyServlet.Route(routeDef, handler));
            } else if (method.isAnnotationPresent(Delete.class)) {
                Delete annotation = method.getAnnotation(Delete.class);
                TinyServlet.RouteDefinition routeDef = TinyServlet.RouteDefinition.fromPath(annotation.value());
                deleteRoutes.add(new TinyServlet.Route(routeDef, handler));
            }
        }

        builder.getRoutes(getRoutes);
        builder.postRoutes(postRoutes);
        builder.putRoutes(putRoutes);
        builder.deleteRoutes(deleteRoutes);

        return builder.build().createServlet();
    }

    private static Function<HttpServletRequest, TinyResponse<?>> createHandler(Object controller, Method method, ObjectMapper objectMapper) {
        return request -> {
            try {
                if (method.getReturnType() != TinyResponse.class) {
                    throw new RuntimeException("Method " + method.getName() + " must return a TinyResponse");
                }

                Object[] args = new Object[method.getParameterCount()];
                Parameter[] parameters = method.getParameters();

                for (int i = 0; i < parameters.length; i++) {
                    Parameter param = parameters[i];
                    if (param.isAnnotationPresent(PathParam.class)) {
                        PathParam pathParam = param.getAnnotation(PathParam.class);
                        Map<String, String> pathParams = (Map<String, String>) request.getAttribute("pathParams");
                        String value = pathParams.get(pathParam.value());
                        args[i] = convert(value, param.getType(), objectMapper);
                    } else if (param.isAnnotationPresent(QueryParam.class)) {
                        QueryParam queryParam = param.getAnnotation(QueryParam.class);
                        String value = request.getParameter(queryParam.value());
                        args[i] = convert(value, param.getType(), objectMapper);
                    } else if (param.isAnnotationPresent(RequestBody.class)) {
                        try {
                            args[i] = objectMapper.readValue(request.getReader()
                                    .lines().collect(Collectors.joining(System.lineSeparator())), param.getType());
                        } catch (IOException e) {
                            System.err.println(e.getMessage());
                            throw new RuntimeException("Error reading request body", e);
                        }
                    } else if (param.getType() == HttpServletRequest.class) {
                        args[i] = request;
                    } else {
                        // Potentially handle other types of parameters here, like @RequestBody
                        args[i] = null;
                    }
                }

                return (TinyResponse<?>) method.invoke(controller, args);

            } catch (Exception e) {
                System.err.println(e.getMessage());
                return TinyResponse.internalServerError("Error invoking endpoint method: " + e.getMessage());
            }
        };
    }

    private static Object convert(String value, Class<?> targetType, ObjectMapper objectMapper) throws JsonProcessingException {
        if (value == null) {
            return null;
        }
        if (targetType == String.class) {
            return value;
        }
        if (targetType == Integer.class || targetType == int.class) {
            return Integer.parseInt(value);
        }
        if (targetType == Long.class || targetType == long.class) {
            return Long.parseLong(value);
        }
        if (targetType == Double.class || targetType == double.class) {
            return Double.parseDouble(value);
        }
        if (targetType == Boolean.class || targetType == boolean.class) {
            return Boolean.parseBoolean(value);
        }
        // If it's not a simple type, assume it's a JSON string for an object
        try {
            return objectMapper.readValue(value, targetType);
        } catch (JsonProcessingException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException("Cannot convert value '" + value + "' to type " + targetType.getName(), e);
        }
    }
}
