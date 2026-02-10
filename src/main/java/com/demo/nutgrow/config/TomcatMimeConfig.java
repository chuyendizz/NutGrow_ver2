package com.demo.nutgrow.config;

import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.MimeMappings;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TomcatMimeConfig {

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatMimeCustomizer() {
        return factory -> {
            MimeMappings mappings = new MimeMappings(MimeMappings.DEFAULT);
            mappings.add("woff2", "font/woff2");
            mappings.add("woff",  "font/woff");
            mappings.add("ttf",   "font/ttf");
            mappings.add("eot",   "application/vnd.ms-fontobject");
            factory.setMimeMappings(mappings);
        };
    }
}
