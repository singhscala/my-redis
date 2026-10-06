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
                "OK",
                handler.handle(
                        List.of("SET", "name", "Prachi")
                )
        );

        assertEquals(
                "Prachi",
                handler.handle(
                        List.of("GET", "name")
                )
        );
    }

    @Test
    void shouldReturnNilForMissingKey() {

        assertEquals(
                null,
                handler.handle(
                        List.of("GET", "name")
                )
        );
    }

    @Test
    void shouldDeleteKey() {

        handler.handle(
                List.of("SET", "name", "Prachi")
        );

        assertEquals(
                "1",
                handler.handle(
                        List.of("DEL", "name")
                )
        );

        assertEquals(
                "(nil)",
                handler.handle(
                        List.of("GET", "name")
                )
        );
    }

    @Test
    void shouldCheckKeyExistence() {

        assertEquals(
                "0",
                handler.handle(
                        List.of("EXISTS", "name")
                )
        );

        handler.handle(
                List.of("SET", "name", "Prachi")
        );

        assertEquals(
                "1",
                handler.handle(
                        List.of("EXISTS", "name")
                )
        );
    }

    @Test
    void shouldExpireKey() throws InterruptedException {

        assertEquals(
                "OK",
                handler.handle(
                        List.of(
                                "SET",
                                "session",
                                "abc123",
                                "EX",
                                "1"
                        )
                )
        );

        assertEquals(
                "abc123",
                handler.handle(
                        List.of("GET", "session")
                )
        );

        Thread.sleep(1100);

        assertEquals(
                "(nil)",
                handler.handle(
                        List.of("GET", "session")
                )
        );
    }

    @Test
    void shouldHandleEmptyCommand() {

        assertEquals(
                "ERR empty command",
                handler.handle(List.of())
        );
    }

    @Test
    void shouldHandleUnknownCommand() {

        assertEquals(
                "ERR unknown command",
                handler.handle(
                        List.of("UNKNOWN")
                )
        );
    }

    @Test
    void shouldHandleWrongArguments() {

        assertEquals(
                "ERR wrong number of arguments",
                handler.handle(
                        List.of("GET")
                )
        );
    }
}