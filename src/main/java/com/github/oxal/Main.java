package com.github.oxal;

import com.github.oxal.annotation.Application;
import com.github.oxal.context.ContextService;
import com.github.oxal.runner.ApplicationRunner;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;

import java.io.File;

@Application
public class Main {

    public static void main(String[] args) throws Exception {
        ApplicationRunner.loadContext(Main.class);
    }
}
