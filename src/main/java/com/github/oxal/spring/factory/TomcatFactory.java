package com.github.oxal.spring.factory;

import com.github.oxal.context.Context;
import com.github.oxal.spring.configuration.ServerConfiguration;
import com.github.oxal.spring.configuration.SpringConfiguration;
import com.github.oxal.spring.enumeration.Endpoint;
import com.github.oxal.spring.servlet.error.ErrorServlet;
import com.github.oxal.spring.utils.UrlUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.ErrorPage;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Slf4j
public class TomcatFactory {

    public static Tomcat create(SpringConfiguration springConfiguration, Context context, List<Class<?>> endpoints, List<ErrorServlet> errorServlets) throws LifecycleException {
        ServerConfiguration serverConfiguration = springConfiguration.getServer();
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(serverConfiguration.getPort());

        String tempDir = System.getProperty("java.io.tmpdir");
        File tomcatBaseDir = new File(tempDir, "tiny-spring-tomcat");
        if (!tomcatBaseDir.exists() && !tomcatBaseDir.mkdirs()) {
            throw new LifecycleException("Failed to create Tomcat base directory at " + tomcatBaseDir.getAbsolutePath());
        }
        tomcat.setBaseDir(tomcatBaseDir.getAbsolutePath());

        org.apache.catalina.Context ctx = tomcat.addContext("", null);

        tomcat.getConnector();
        configError(ctx, errorServlets);

        for (Class<?> endpoint : endpoints) {
            String baseUrl = UrlUtils.buildUrl(serverConfiguration.getBaseUrl(), endpoint.getAnnotation(Endpoint.class).baseUrl());
            String servletName = endpoint.getName();

            Tomcat.addServlet(ctx, servletName, EndPointFactory.buildEndpoint(endpoint, context));
            ctx.addServletMappingDecoded(baseUrl + "/*", servletName);
            log.info("Mapping {}/* to {}", baseUrl, servletName);
        }
        log.info("Server started on port {}", serverConfiguration.getPort());

        return tomcat;
    }

    private static void configError(org.apache.catalina.Context ctx, List<ErrorServlet> errorServlets) {
        for (ErrorServlet errorServlet : errorServlets) {
            HttpServlet servlet = new HttpServlet() {
                @Override
                protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
                    errorServlet.getServlet().accept(req, resp);
                }
            };
            Tomcat.addServlet(ctx, errorServlet.getName(), servlet);
            String errorPath = String.format("/%s", errorServlet.getUrl());
            ctx.addServletMappingDecoded(errorPath, errorServlet.getName());

            ErrorPage error = new ErrorPage();
            error.setErrorCode(errorServlet.getCode());
            error.setLocation(errorPath);
            ctx.addErrorPage(error);
        }
    }
}
