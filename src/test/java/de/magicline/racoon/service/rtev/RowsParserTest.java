package de.magicline.racoon.service.rtev;

import de.magicline.racoon.service.task.RowValue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;


@ExtendWith(MockitoExtension.class)
class RowsParserTest {

    private RowsParser testee = new RowsParser();

    @Test
    void parse() throws IOException {
        String text = String.join("\n",
                "email,result,message",
                "a@a.pl,410,Address Rejected",
                "info@magicline.de,200,OK - Valid Address");
        InputStream data = IOUtils.toInputStream(text, StandardCharsets.UTF_8);

        List<RowValue> result = testee.parse(data);

        assertThat(result)
                .extracting(RowValue::getEmail, RowValue::getResult, RowValue::getMessage)
                .containsExactly(
                        tuple("a@a.pl", 410, "Address Rejected"),
                        tuple("info@magicline.de", 200, "OK - Valid Address"));
    }

}
