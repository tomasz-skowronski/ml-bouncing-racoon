package de.magicline.racoon.service.rtev;

import feign.Logger;
import feign.auth.BasicAuthRequestInterceptor;
import feign.codec.ErrorDecoder;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

//@Configuration
public class RTEVConfiguration {

    @Bean
    public BasicAuthRequestInterceptor basicAuthRequestInterceptor() {
        return null;
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return null;
    }

    @Bean
    Logger.Level feignLoggerLevel(@Value("${feign.client.config.default.loggerLevel:FULL}") String debugLevel) {
        return Logger.Level.valueOf(debugLevel);
    }
}
