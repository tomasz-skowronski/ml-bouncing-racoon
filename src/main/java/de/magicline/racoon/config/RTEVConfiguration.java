package de.magicline.racoon.config;

import de.magicline.racoon.service.rtev.RTEVValidationClient;
import feign.Feign;
import feign.Retryer;
import feign.form.FormEncoder;
import feign.jackson.JacksonDecoder;
import feign.slf4j.Slf4jLogger;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RTEVConfiguration {

    public static final String URI_ONE = "https://api.email-validator.net";
    public static final String URI_ASYNC = "https://bulk.email-validator.net";
    public static final String URI_DOWNLOAD = "https://www.email-validator.net";

    private final URI uriOne;
    private final URI uriAsync;
    private final URI uriDownload;
    private String notifyURL;
    private String apiKey;

    public RTEVConfiguration(
            @Value("${app.rtev.uri.one:" + URI_ONE + "}") String uriOne,
            @Value("${app.rtev.uri.async:" + URI_ASYNC + "}") String uriAsync,
            @Value("${app.rtev.uri.download:" + URI_DOWNLOAD + "}") String uriDownload,
            @Value("${app.rtev.apiKey") String apiKey,
            @Value("${app.rtev.notifyURL") String notifyURL
    ) {
        this.uriOne = URI.create(uriOne);
        this.uriAsync = URI.create(uriAsync);
        this.uriDownload = URI.create(uriDownload);
        this.notifyURL = notifyURL;
        this.apiKey = apiKey;
    }

    @Bean
    public RTEVValidationClient rtevValidationClient() {
        return Feign.builder()
                .encoder(new FormEncoder())
                .decoder(new JacksonDecoder())
                .retryer(Retryer.NEVER_RETRY)
                .logger(new Slf4jLogger())
                .logLevel(feign.Logger.Level.FULL)
                .target(RTEVValidationClient.class, "ignore it");
    }

    public URI getUriOne() {
        return uriOne;
    }

    public URI getUriAsync() {
        return uriAsync;
    }

    public URI getUriDownload() {
        return uriDownload;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getNotifyURL() {
        return notifyURL;
    }
}
