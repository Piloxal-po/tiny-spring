package com.github.oxal.spring.configuration;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServerConfiguration {
    private int port = 8080;
    private String baseUrl = "";
    private String errorUrl = "error";
}
