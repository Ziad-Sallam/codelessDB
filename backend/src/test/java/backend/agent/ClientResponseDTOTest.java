package backend.agent;

import backend.agent.WebSocketHandler.*;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

class ClientResponseDTOTest {

    /* --------------------------------------------------------
       Default constructor + setters
     -------------------------------------------------------- */

    @Test
    void defaultConstructor_andSetters_workCorrectly() {
        ClientResponseDTO dto = new ClientResponseDTO();

        dto.setCorrelationId("corr-1");
        dto.setType("SELECT");
        dto.setColumns(List.of("id", "name"));
        dto.setRows(List.of(List.of(1, "Alice")));
        dto.setRowCount(1);
        dto.setSuccess(true);
        dto.setMessage("OK");

        assertEquals("corr-1", dto.getCorrelationId());
        assertEquals("SELECT", dto.getType());
        assertEquals(1, dto.getRowCount());
        assertTrue(dto.isSuccess());
        assertEquals("OK", dto.getMessage());
    }

    /* --------------------------------------------------------
       SELECT constructor
     -------------------------------------------------------- */

    @Test
    void selectConstructor_setsAllFields_andDefaultMessage() {
        ClientResponseDTO dto = new ClientResponseDTO(
                "corr-2",
                "SELECT",
                List.of("id"),
                List.of(List.of(1)),
                1,
                true
        );

        assertEquals("corr-2", dto.getCorrelationId());
        assertEquals("SELECT", dto.getType());
        assertEquals(List.of("id"), dto.getColumns());
        assertEquals(1, dto.getRowCount());
        assertTrue(dto.isSuccess());
        assertEquals("Query executed successfully.", dto.getMessage());
    }

    /* --------------------------------------------------------
       Non-SELECT constructor
     -------------------------------------------------------- */

    @Test
    void nonSelectConstructor_setsAllFields() {
        ClientResponseDTO dto = new ClientResponseDTO(
                "corr-3",
                true,
                "UPDATE",
                5,
                "5 rows updated"
        );

        assertEquals("corr-3", dto.getCorrelationId());
        assertEquals("UPDATE", dto.getType());
        assertEquals(5, dto.getRowCount());
        assertTrue(dto.isSuccess());
        assertEquals("5 rows updated", dto.getMessage());
    }

    /* --------------------------------------------------------
       Error constructor
     -------------------------------------------------------- */

    @Test
    void errorConstructor_setsErrorType() {
        ClientResponseDTO dto = new ClientResponseDTO(
                "corr-4",
                false,
                "Syntax error"
        );

        assertEquals("corr-4", dto.getCorrelationId());
        assertFalse(dto.isSuccess());
        assertEquals("ERROR", dto.getType());
        assertEquals("Syntax error", dto.getMessage());
    }

    /* --------------------------------------------------------
       toString
     -------------------------------------------------------- */

    @Test
    void toString_containsImportantFields() {
        ClientResponseDTO dto = new ClientResponseDTO(
                "corr-5",
                false,
                "Failure occurred"
        );

        String str = dto.toString();

        assertTrue(str.contains("corr-5"));
        assertTrue(str.contains("ERROR"));
        assertTrue(str.contains("Failure occurred"));
    }
}
