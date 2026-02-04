package com.github.oxal.spring.enumeration;

import com.github.oxal.annotation.Bean;
import com.github.oxal.annotation.ScopeType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Bean
public @interface Endpoint {
    String value() default "DEFAULT";
    ScopeType scope() default ScopeType.SINGLETON;
    String baseUrl() default "";
}
