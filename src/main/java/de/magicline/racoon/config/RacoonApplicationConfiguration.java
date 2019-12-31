package de.magicline.racoon.config;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RacoonApplicationConfiguration {

    public  static final ZoneId ZONE_ID = ZoneId.of("Europe/Berlin");

    @Bean
    Clock clock() {
        return Clock.system(ZONE_ID);
    }

}
