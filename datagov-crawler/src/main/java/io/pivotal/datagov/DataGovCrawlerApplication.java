package io.pivotal.datagov;

import io.pivotal.datagov.component.ServiceProperties;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@EnableAutoConfiguration
@EnableConfigurationProperties(ServiceProperties.class)
@ComponentScan
@ImportResource("classpath:META-INF/spring/datagov-crawler.xml")
public class DataGovCrawlerApplication {

	public static void main(String[] args) {
		SpringApplication.run(DataGovCrawlerApplication.class, args);
    }
}
