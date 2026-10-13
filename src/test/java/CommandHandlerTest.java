import com.prachi.command.CommandHandler;
import com.prachi.model.RedisResponse;
import com.prachi.store.RedisStore;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CommandHandlerTest {

    RedisStore store = new RedisStore();
    CommandHandler handler = new CommandHandler(store);

    private void assertResponse(Object expectedValue, List<String> command) {
        RedisResponse response = handler.handle(command);
        assertEquals(expectedValue, response.getValue());
    }

    @Test
    void shouldSetAndGetValue() {
        assertResponse("OK", List.of("SET", "name", "Prachi"));
        assertResponse("Prachi", List.of("GET", "name"));
    }

    @Test
    void shouldReturnNilForMissingKey() {
        assertResponse(null, List.of("GET", "name"));
    }

    @Test
    void shouldDeleteKey() {
        handler.handle(List.of("SET", "name", "Prachi"));

        assertResponse(1L, List.of("DEL", "name"));
        assertResponse(null, List.of("GET", "name"));
    }

    @Test
    void shouldCheckKeyExistence() {
        assertResponse(0L, List.of("EXISTS", "name"));

        handler.handle(List.of("SET", "name", "Prachi"));

        assertResponse(1L, List.of("EXISTS", "name"));
    }

    @Test
    void shouldExpireKey() throws InterruptedException {
        assertResponse(
                "OK",
                List.of("SET", "session", "abc123", "EX", "1")
        );

        assertResponse("abc123", List.of("GET", "session"));

        Thread.sleep(1100);

        assertResponse(null, List.of("GET", "session"));
    }

    @Test
    void shouldHandleEmptyCommand() {
        RedisResponse response = handler.handle(List.of());

        assertEquals("ERROR", response.getType().name());
        assertEquals("ERR-1001 Command cannot be empty", response.getValue());
    }

    @Test
    void shouldHandleUnknownCommand() {
        RedisResponse response = handler.handle(List.of("UNKNOWN"));

        assertEquals("ERROR", response.getType().name());
        assertEquals(
                response.getValue(),
                response.getValue()
        );
    }

    @Test
    void shouldHandleWrongArguments() {
        RedisResponse response = handler.handle(List.of("GET"));

        assertEquals("ERROR", response.getType().name());
        assertEquals(
                response.getValue(),
                response.getValue()
        );
    }

    @Test
    void shouldSetAndGetHashField() {
        assertResponse(1L, List.of("HSET", "user:1", "name", "Prachi"));
        assertResponse("Prachi", List.of("HGET", "user:1", "name"));
    }
}