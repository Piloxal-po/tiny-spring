package com.github.oxal.spring.context;

import com.github.oxal.annotation.context.AfterContextLoad;
import com.github.oxal.context.Context;
import com.github.oxal.spring.enumeration.Endpoint;
import com.github.oxal.spring.service.EndPointService;
import io.github.classgraph.ScanResult;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;

import java.io.File;
import java.util.List;

public class LoadContext {

    @AfterContextLoad(order = Integer.MIN_VALUE)
    public void afterContextLoad(ScanResult scanResult, Context context) {
        List<Class<?>> endpoints = scanResult.getClassesWithAnnotation(Endpoint.class).loadClasses(true);
        for (Class<?> endpoint : endpoints) {
            if (context.getBeanDefinitionKey(endpoint, null).isPresent()) {
                System.out.printf("%s is loaded %n", endpoint.getName());
            } else {
                System.err.printf("%s is not loaded %n", endpoint.getSimpleName());
            }
        }
    }

    @AfterContextLoad(order = Integer.MAX_VALUE)
    public void afterContextLoadApache(ScanResult scanResult, Context context) throws LifecycleException {
        List<Class<?>> endpoints = scanResult.getClassesWithAnnotation(Endpoint.class).loadClasses(true);
        System.out.printf("%d endpoints found%n", endpoints.size());

        Tomcat tomcat = new Tomcat();
        tomcat.setPort(8080);
        tomcat.getConnector(); // This is needed to initialize the connector

        org.apache.catalina.Context ctx = tomcat.addContext("", new File(".").getAbsolutePath());

        for (Class<?> endpoint : endpoints) {
            String baseUrl = endpoint.getAnnotation(Endpoint.class).baseUrl();
            String servletName = endpoint.getName();
            
            Tomcat.addServlet(ctx, servletName, EndPointService.buildEndpoint(endpoint, context));
            ctx.addServletMappingDecoded(baseUrl + "/*", servletName);
            System.out.printf("Mapping %s/* to %s%n", baseUrl, servletName);
        }

        tomcat.start();
        tomcat.getServer().await();
    }
}
