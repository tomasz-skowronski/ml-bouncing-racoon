package de.magicline.racoon;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@DirtiesContext
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class RacoonApplicationIT {

    @Test
    void contextLoads() {
        Assertions.assertTrue(true);
    }

}
