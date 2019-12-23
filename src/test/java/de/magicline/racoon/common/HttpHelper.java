package de.magicline.racoon.common;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import com.github.tomakehurst.wiremock.matching.RegexPattern;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;

public final class HttpHelper {

    private HttpHelper() {
        super();
    }

    public static StringValuePattern hasFormParam(String key, String value) {
        return new RegexPattern("(.*&|^)"
                + key
                + "="
                + URLEncoder.encode(value, StandardCharsets.UTF_8)
                + "($|&.*)");
    }

}
