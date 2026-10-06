import com.prachi.protocol.RespParser;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class RespParserTest {

    @Test
    void shouldParseBulkString() {

        RespParser parser = new RespParser();

        String result =
                parser.parseBulkString("$5\r\nhello\r\n");

        assertEquals("hello", result);
    }

    @Test
    void shouldParseArray() {

        RespParser parser = new RespParser();

        String input =
                "*3\r\n" +
                        "$3\r\n" +
                        "SET\r\n" +
                        "$4\r\n" +
                        "name\r\n" +
                        "$6\r\n" +
                        "prachi\r\n";

        List<String> result = parser.parseArray(input);

        assertEquals(
                List.of("SET", "name", "prachi"),
                result
        );
    }

    @Test
    void shouldParseSimpleString() {

        RespParser parser = new RespParser();

        String result =
                parser.parseSimpleString("+OK\r\n");

        assertEquals("OK", result);
    }

    @Test
    void shouldParseError() {

        RespParser parser = new RespParser();

        String result =
                parser.parseError("-ERR unknown command\r\n");

        assertEquals("ERR unknown command", result);
    }

    @Test
    void shouldParseNull() {

        RespParser parser = new RespParser();

        String result =
                parser.parseNull("$-1\r\n");

        assertNull(result);
    }

    @Test
    void shouldReadBulkStringFromInputStream() throws IOException {

        RespParser parser = new RespParser();

        String input = "$6\r\nprachi\r\n";

        InputStream inputStream =
                new ByteArrayInputStream(
                        input.getBytes()
                );

        String result =
                parser.readBulkString(inputStream);

        assertEquals("prachi", result);
    }

    @Test
    void shouldReadArrayFromInputStream() throws IOException {

        RespParser parser = new RespParser();

        String input =
                "*3\r\n" +
                        "$3\r\n" +
                        "SET\r\n" +
                        "$4\r\n" +
                        "name\r\n" +
                        "$6\r\n" +
                        "prachi\r\n";

        InputStream inputStream =
                new ByteArrayInputStream(
                        input.getBytes()
                );

        List<String> result =
                parser.readArray(inputStream);

        assertEquals(
                List.of("SET", "name", "prachi"),
                result
        );
    }
}
