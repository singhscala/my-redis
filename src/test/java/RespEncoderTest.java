import com.prachi.protocol.RespEncoder;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RespEncoderTest {

    @Test
    void shouldEncodeSimpleString() {

        RespEncoder encoder = new RespEncoder();

        String result =
                encoder.encodeSimpleString("OK");

        assertEquals("+OK\r\n", result);
    }

    @Test
    void shouldEncodeError() {

        RespEncoder encoder = new RespEncoder();

        String result =
                encoder.encodeError("ERR unknown command");

        assertEquals(
                "-ERR unknown command\r\n",
                result
        );
    }

    @Test
    void shouldEncodeBulkString() {

        RespEncoder encoder = new RespEncoder();

        String result =
                encoder.encodeBulkString("prachi");

        assertEquals(
                "$6\r\nprachi\r\n",
                result
        );
    }

    @Test
    void shouldEncodeNull() {

        RespEncoder encoder = new RespEncoder();

        String result =
                encoder.encodeBulkString(null);

        assertEquals(
                "$-1\r\n",
                result
        );
    }

    @Test
    void shouldEncodeArray() {

        RespEncoder encoder = new RespEncoder();

        String result =
                encoder.encodeArray(
                        List.of("SET", "name", "prachi")
                );

        assertEquals(
                "*3\r\n" +
                        "$3\r\n" +
                        "SET\r\n" +
                        "$4\r\n" +
                        "name\r\n" +
                        "$6\r\n" +
                        "prachi\r\n",
                result
        );
    }

    @Test
    void shouldEncodeInteger() {

        RespEncoder encoder = new RespEncoder();

        assertEquals(
                ":1\r\n",
                encoder.encodeInteger(1)
        );

        assertEquals(
                ":0\r\n",
                encoder.encodeInteger(0)
        );
    }
}
