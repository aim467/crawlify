package org.crawlify.platform.config;

import org.pf4j.DefaultPluginManager;
import org.pf4j.PluginManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Paths;

@Configuration
public class Pf4jConfig {

    @Value("${crawlify.jar-path-prefix}")
    private String jarPathPrefix;

    @Bean
    public PluginManager pluginManager() {
        return new DefaultPluginManager(Paths.get(jarPathPrefix));
    }

}
