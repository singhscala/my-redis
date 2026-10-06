import com.prachi.command.CommandHandler;
import com.prachi.store.RedisStore;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CommandHandlerTest {

    RedisStore store = new RedisStore();
    CommandHandler handler = new CommandHandler(store);

    @Test
    void shouldSetAndGetValue() {

        assertEquals(
                "Ok",
                handler.handle("SET name Prachi")
        );

        assertEquals(
                "Prachi",
                handler.handle("GET name")
        );
    }

    @Test
    void shouldReturnNilForMissingKey() {

        assertEquals(
                "(nil)",
                handler.handle("GET name")
        );
    }

    @Test
    void shouldDeleteKey() {

        handler.handle("SET name Prachi");

        assertEquals(
                "1",
                handler.handle("DEL name")
        );

        assertEquals(
                "(nil)",
                handler.handle("GET name")
        );
    }

    @Test
    void shouldCheckKeyExistence() {

        assertEquals(
                "0",
                handler.handle("EXISTS name")
        );

        handler.handle("SET name Prachi");

        assertEquals(
                "1",
                handler.handle("EXISTS name")
        );
    }

    @Test
    void shouldExpireKey() throws InterruptedException {

        assertEquals(
                "OK",
                handler.handle("SET session abc123 EX 1")
        );

        assertEquals(
                "abc123",
                handler.handle("GET session")
        );

        Thread.sleep(1100);

        assertEquals(
                "(nil)",
                handler.handle("GET session")
        );
    }

    @Test
    void shouldHandleRespCommand() {

        String result =
                handler.handle(
                        List.of("SET", "name", "prachi")
                );

        assertEquals("OK", result);
    }

    @Test
    void shouldGetValueFromRespCommand() {

        handler.handle(
                List.of("SET", "name", "prachi")
        );

        String result =
                handler.handle(
                        List.of("GET", "name")
                );

        assertEquals("prachi", result);
    }
}
