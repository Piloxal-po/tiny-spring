package com.github.oxal.spring.context;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.oxal.annotation.context.AfterContextLoad;
import com.github.oxal.context.Context;
import com.github.oxal.factory.BeanFactory;
import com.github.oxal.object.KeyDefinition;
import com.github.oxal.spring.configuration.SpringConfiguration;
import com.github.oxal.spring.enumeration.Endpoint;
import com.github.oxal.spring.factory.TomcatFactory;
import com.github.oxal.spring.servlet.error.ErrorServlet;
import io.github.classgraph.ScanResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;

import java.util.List;

@Slf4j
public class LoadContext {
    public static final int TOMCAT_LOAD = Integer.MAX_VALUE - 1000;
    public static final int TOMCAT_RUN = Integer.MAX_VALUE;

    @AfterContextLoad(order = Integer.MIN_VALUE)
    public void afterContextLoad(ScanResult scanResult) {
        List<Class<?>> endpoints = scanResult.getClassesWithAnnotation(Endpoint.class).loadClasses(true);
        for (Class<?> endpoint : endpoints) {
            if (BeanFactory.loadBean(endpoint) != null) {
                log.info("{} is loaded", endpoint.getName());
            } else {
                log.warn("{} is not loaded", endpoint.getSimpleName());
            }
        }
    }

    @AfterContextLoad(order = TOMCAT_LOAD)
    public void afterContextLoadApache(ScanResult scanResult, Context context, SpringConfiguration springConfiguration, List<ErrorServlet> errorServlets) throws LifecycleException {
        List<Class<?>> endpoints = scanResult.getClassesWithAnnotation(Endpoint.class).loadClasses(true);
        log.info("{} endpoints found", endpoints.size());

        if (context.getBeanDefinitions()
                .keySet()
                .stream().noneMatch(keyDefinition -> keyDefinition.getType().equals(ObjectMapper.class))) {
            log.debug("Registering default ObjectMapper");
            context.addBeanDefinitionByMethod(LoadContext.class, ObjectMapper.class, "objectMapper");
        }

        Tomcat tomcat = TomcatFactory.create(springConfiguration, context, endpoints, errorServlets);
        context.registerSingleton(KeyDefinition.builder().type(Tomcat.class).build(), tomcat);
    }

    @AfterContextLoad(order = TOMCAT_RUN)
    public void afterContextRunApache(Tomcat tomcat) throws LifecycleException {
        tomcat.start();
        tomcat.getServer().await();
    }

    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
