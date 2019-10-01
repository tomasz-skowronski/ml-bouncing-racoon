package de.magicline.racoon.service.rtev;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

public class RowsParser {

    private final CsvMapper mapper;

    public RowsParser() {
        this.mapper = new CsvMapper();
    }

    public List<RowValue> parse(InputStream inputStream) throws IOException {
        return mapper.readerFor(RowValue.class)
                .with(CsvSchema.emptySchema().withHeader())
                .<RowValue>readValues(inputStream)
                .readAll();
    }
}
