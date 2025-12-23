package backend.databaseManagement.exception;

import backend.config.ErrorResponse;
import backend.databaseManagement.exception.DatabaseException.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class DatabaseExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new TestController())
                .setControllerAdvice(new DatabaseExceptionHandler())
                .build();
    }

    // --- Tests ---

    @Test
    void handleDatabaseNotFound() throws Exception {
        mockMvc.perform(get("/db-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Database not found"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void handleDatabaseNotConnected() throws Exception {
        mockMvc.perform(get("/db-not-connected"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value("Database not connected"))
                .andExpect(jsonPath("$.status").value(503));
    }

    @Test
    void handleServerNotFound() throws Exception {
        mockMvc.perform(get("/server-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Server not found"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void handleDatabaseAlreadyExists() throws Exception {
        mockMvc.perform(get("/db-already-exists"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Database already exists"))
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void handleServerAlreadyExists() throws Exception {
        mockMvc.perform(get("/server-already-exists"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Server already exists"))
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void handleMissingField() throws Exception {
        mockMvc.perform(get("/missing-field"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Missing field"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void handleUnauthorizedAccess() throws Exception {
        mockMvc.perform(get("/unauthorized"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Unauthorized"))
                .andExpect(jsonPath("$.status").value(403));
    }


    @RestController
    static class TestController {

        @GetMapping("/db-not-found")
        public void dbNotFound() {
            throw new DatabaseNotFoundException("Database not found");
        }

        @GetMapping("/db-not-connected")
        public void dbNotConnected() {
            throw new DatabaseNotConnectedException("Database not connected");
        }

        @GetMapping("/server-not-found")
        public void serverNotFound() {
            throw new ServerNotFoundException("Server not found");
        }

        @GetMapping("/db-already-exists")
        public void dbAlreadyExists() {
            throw new DatabaseAlreadyExistsException("Database already exists");
        }

        @GetMapping("/server-already-exists")
        public void serverAlreadyExists() {
            throw new ServerAlreadyExistsException("Server already exists");
        }

        @GetMapping("/missing-field")
        public void missingField() {
            throw new MissingFieldException("Missing field");
        }

        @GetMapping("/unauthorized")
        public void unauthorized() {
            throw new UnauthorizedAccessException("Unauthorized");
        }
    }
}
