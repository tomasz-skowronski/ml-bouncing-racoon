package de.magicline.racoon.service.rtev;

import de.magicline.racoon.service.task.RowValue;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

@Component
public class RowsParser {

    private final CsvMapper mapper;

    public RowsParser() {
        this.mapper = new CsvMapper();
    }

    public List<RowValue> parse(InputStream inputStream) throws IOException {
        return mapper.readerFor(RTEVRowValue.class)
                .with(CsvSchema.emptySchema().withHeader())
                .<RowValue>readValues(inputStream)
                .readAll();
    }
}
