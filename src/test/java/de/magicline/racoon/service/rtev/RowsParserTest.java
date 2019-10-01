package de.magicline.racoon.service.rtev;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

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

        assertThat(result).containsExactly(
                new RowValue("a@a.pl", 410, "Address Rejected"),
                new RowValue("info@magicline.de", 200, "OK - Valid Address"));
    }

}
