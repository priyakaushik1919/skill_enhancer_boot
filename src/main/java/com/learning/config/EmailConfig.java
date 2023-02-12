package com.learning.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix ="email-config" )
public class EmailConfig {

    private String message;
    private String subject;
    private String attachment;
    private String from;

}