package de.magicline.racoon.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PingController {

    @GetMapping("/")
    public String root() {
        return "OK";
    }

    @GetMapping("/ping")
    public String index() {
        return "pong";
    }
}
