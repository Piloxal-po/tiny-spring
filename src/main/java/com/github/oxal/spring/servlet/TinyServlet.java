package com.github.oxal.spring.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.*;

import java.io.IOException;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TinyServlet {

    private ObjectMapper objectMapper;

    @Builder.Default
    private List<Route> getRoutes = new ArrayList<>();
    @Builder.Default
    private List<Route> postRoutes = new ArrayList<>();
    @Builder.Default
    private List<Route> putRoutes = new ArrayList<>();
    @Builder.Default
    private List<Route> deleteRoutes = new ArrayList<>();

    public record RouteDefinition(Pattern pattern, List<String> paramNames) {
        public static RouteDefinition fromPath(String path) {
            List<String> paramNames = new ArrayList<>();
            Pattern paramPattern = Pattern.compile("\\{([^}]+)}");
            Matcher paramMatcher = paramPattern.matcher(path);
            while (paramMatcher.find()) {
                paramNames.add(paramMatcher.group(1));
            }
            String regexPath = path.replaceAll("\\{[^}]+}", "([^/]+)");
            return new RouteDefinition(Pattern.compile("^" + regexPath + "$"), paramNames);
        }
    }

    public record Route(RouteDefinition definition, Function<HttpServletRequest, TinyResponse<?>> handler) {
    }

    public HttpServlet createServlet() {
        return new HttpServlet() {
            @Override
            protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
                handleRequest(getRoutes, req, resp);
            }

            @Override
            protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
                handleRequest(postRoutes, req, resp);
            }

            @Override
            protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
                handleRequest(putRoutes, req, resp);
            }

            @Override
            protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
                handleRequest(deleteRoutes, req, resp);
            }
        };
    }

    private void handleRequest(List<Route> routes, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo() == null ? "/" : req.getPathInfo();

        for (Route route : routes) {
            Matcher matcher = route.definition().pattern().matcher(path);
            if (matcher.matches()) {
                Map<String, String> pathParams = new HashMap<>();
                List<String> paramNames = route.definition().paramNames();
                for (int i = 0; i < paramNames.size(); i++) {
                    pathParams.put(paramNames.get(i), matcher.group(i + 1));
                }
                req.setAttribute("pathParams", pathParams);

                TinyResponse<?> res = route.handler().apply(req);
                buildResponse(resp, res);
                return;
            }
        }

        buildResponse(resp, TinyResponse.notFound(null));
    }

    private void buildResponse(HttpServletResponse resp, TinyResponse<?> res) throws IOException {
        resp.setContentType(res.getContentType());
        resp.setCharacterEncoding(res.getEncoding());
        resp.setStatus(res.getStatut());
        if (res.getHeaderString() != null) {
            res.getHeaderString().forEach(resp::addHeader);
        }
        if (res.getHeaderDate() != null) {
            res.getHeaderDate().forEach((k, v) -> resp.addDateHeader(k, v.getLong(ChronoField.INSTANT_SECONDS)));
        }
        if (res.getHeaderInt() != null) {
            res.getHeaderInt().forEach(resp::setIntHeader);
        }
        if (res.getCookies() != null) {
            res.getCookies().forEach((k, v) -> resp.addCookie(new Cookie(k, v)));
        }
        if (res.getData() != null) {
            if (res.getData() instanceof String data) {
                resp.getWriter().write(data);
            } else {
                resp.getWriter().write(objectMapper.writeValueAsString(res.getData()));
            }
        }
    }
}
