package de.magicline.racoon.common;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.matching.RegexPattern;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;

public final class HttpHelper {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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

    public static <T> String toJson(T body) throws JsonProcessingException {
        return OBJECT_MAPPER.writeValueAsString(body);
    }

}
