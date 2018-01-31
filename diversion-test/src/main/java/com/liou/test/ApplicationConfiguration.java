package com.liou.test;

import org.springframework.context.annotation.Configuration;

import com.liou.diversion.EnableDiversion;

@Configuration
@EnableDiversion(locations = "classpath:diversion.properties")
public class ApplicationConfiguration {
}
