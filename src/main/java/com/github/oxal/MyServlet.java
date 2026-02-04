package com.github.oxal;

import com.github.oxal.context.Context;
import com.github.oxal.context.ContextService;
import com.github.oxal.spring.enumeration.Endpoint;
import com.github.oxal.spring.enumeration.operator.Get;
import com.github.oxal.spring.enumeration.param.PathParam;
import com.github.oxal.spring.enumeration.param.QueryParam;
import com.github.oxal.spring.servlet.TinyResponse;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@Endpoint
public class MyServlet {

    @Get
    public TinyResponse<String[]> get() {
        return TinyResponse.ok(ContextService.getContext().getPackages());
    }

    @Get("/path/{param}")
    public TinyResponse<String> getTest(@PathParam("param") String param, @QueryParam("user") String user) {
        return TinyResponse.ok(param + " : " + user);
    }
}
