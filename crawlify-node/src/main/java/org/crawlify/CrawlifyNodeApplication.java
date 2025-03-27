package org.crawlify;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("org.crawlify.common.mapper")
public class CrawlifyNodeApplication {

    public static void main(String[] args) {
        SpringApplication.run(CrawlifyNodeApplication.class, args);
    }

}
