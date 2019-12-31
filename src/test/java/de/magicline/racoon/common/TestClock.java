package de.magicline.racoon.common;

import de.magicline.racoon.config.RacoonApplicationConfiguration;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

public class TestClock extends Clock {

    @Override
    public ZoneId getZone() {
        return RacoonApplicationConfiguration.ZONE_ID;
    }

    @Override
    public Clock withZone(ZoneId zoneId) {
        return Clock.fixed(Instant.EPOCH, getZone());
    }

    @Override
    public Instant instant() {
        return withZone(getZone()).instant();
    }
}
