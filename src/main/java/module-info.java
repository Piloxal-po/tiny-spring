/**
 * Defines the module for the Tiny Spring application.
 */
module com.github.oxal.tinyspring {
    // --- Dependencies ---

    // Dependency for the core bean framework
    // Assuming 'tiny.bean' is the automatic module name for the tiny-bean library.
    requires tiny.bean;

    // Dependency for JSON processing
    requires com.fasterxml.jackson.databind;

    // Dependency for the embedded Tomcat server
    requires org.apache.tomcat.embed.core;

    requires io.github.classgraph;
    requires org.slf4j;

    // Lombok is a compile-time dependency, 'static' makes it optional at runtime.
    requires static lombok;
    requires ch.qos.logback.core;

    // --- Service Provision ---

    // Declare that this module provides an implementation of PackageProvider.
    // This is the fix for the ServiceLoader issue.
    provides com.github.oxal.provider.PackageProvider with com.github.oxal.spring.provider.TinySpringProvider;

    // --- Package Opening for Reflection ---

    // The tiny-bean framework likely uses reflection to find and instantiate beans/controllers.
    // We need to open the packages containing these classes.
    opens com.github.oxal.spring.provider;
    opens com.github.oxal.spring.factory;
    opens com.github.oxal.spring.servlet;
    opens com.github.oxal.spring.servlet.error;
    opens com.github.oxal.spring.enumeration;
    opens com.github.oxal.spring.enumeration.param;
    opens com.github.oxal.spring.enumeration.operator;
    opens com.github.oxal.spring.enumeration.utils;
    opens com.github.oxal.spring.configuration;

    exports com.github.oxal.spring.factory;
    exports com.github.oxal.spring.enumeration;
    exports com.github.oxal.spring.enumeration.param;
    exports com.github.oxal.spring.enumeration.operator;
    exports com.github.oxal.spring.enumeration.utils;
    exports com.github.oxal.spring.context;
    exports com.github.oxal.spring.servlet;
    exports com.github.oxal.spring.servlet.error;
    exports com.github.oxal.spring.configuration;
}
