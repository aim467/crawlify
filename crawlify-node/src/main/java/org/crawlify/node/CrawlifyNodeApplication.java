package org.crawlify.node;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("org.crawlify.node.mapper")
public class CrawlifyNodeApplication {

    public static void main(String[] args) {
        SpringApplication.run(CrawlifyNodeApplication.class, args);
    }

}
