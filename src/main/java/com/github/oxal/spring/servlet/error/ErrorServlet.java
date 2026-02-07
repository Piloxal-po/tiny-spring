package com.github.oxal.spring.servlet.error;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.function.BiConsumer;

@Getter
@Setter
@Builder
public class ErrorServlet {
    private String url;
    private String name;
    private int code;

    @Builder.Default
    private BiConsumer<ServletRequest, ServletResponse> servlet = (req, res) -> {
    };
}
