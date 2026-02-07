package com.github.oxal.spring.configuration;

import com.github.oxal.annotation.Configuration;
import lombok.Getter;
import lombok.Setter;

@Configuration(prefix = "spring")
@Getter
@Setter
public class SpringConfiguration {
    private ServerConfiguration server = new ServerConfiguration();
}
