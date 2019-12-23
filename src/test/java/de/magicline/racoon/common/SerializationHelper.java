package de.magicline.racoon.common;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

public final class SerializationHelper {

    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    private static final CsvMapper CSV_MAPPER = new CsvMapper();

    private SerializationHelper() {
        super();
    }

    public static <T> String toJson(T body) throws JsonProcessingException {
        return JSON_MAPPER.writeValueAsString(body);
    }

    @SafeVarargs
    public static <T> String toCsv(Class<T> pojoType, T... pojos) throws IOException {
        CsvSchema schema = CSV_MAPPER.schemaFor(pojoType).withHeader();
        return CSV_MAPPER.writer(schema)
                .writeValueAsString(pojos);
    }

}
