package com.altimetrik.elab.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.altimetrik.elab.demo.service.ComponentDetailsService;

/**
 * @author skondapalli
 */
@EnableAsync
@EnableScheduling
@SpringBootApplication(scanBasePackages = { "com.altimetrik" })
public class Application {

	private static ConfigurableApplicationContext context;

	public static void main(final String[] args) {
		context = SpringApplication.run(Application.class, args);
		context.getBean(ComponentDetailsService.class)
				.createComponentDetails(context.getEnvironment().getProperty("spring.application.name"));
	}

}
