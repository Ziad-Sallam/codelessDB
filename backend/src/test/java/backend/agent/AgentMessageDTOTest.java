package backend.agent;

import backend.agent.WebSocketHandler.*;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class AgentMessageDTOTest {

    /* --------------------------------------------------------
       Default constructor + setters
     -------------------------------------------------------- */

    @Test
    void defaultConstructor_andSetters_workCorrectly() {
        AgentMessageDTO dto = new AgentMessageDTO();

        dto.setSender("agent");
        dto.setContent("hello");
        dto.setCorrelationId("corr-1");

        assertEquals("agent", dto.getSender());
        assertEquals("hello", dto.getContent());
        assertEquals("corr-1", dto.getCorrelationId());
    }

    /* --------------------------------------------------------
       Full constructor
     -------------------------------------------------------- */

    @Test
    void constructor_withExplicitCorrelationId_setsAllFields() {
        AgentMessageDTO dto =
                new AgentMessageDTO("agent", "execute", "corr-2");

        assertEquals("agent", dto.getSender());
        assertEquals("execute", dto.getContent());
        assertEquals("corr-2", dto.getCorrelationId());
    }

    /* --------------------------------------------------------
       Auto-generated correlationId constructor
     -------------------------------------------------------- */

    @Test
    void constructor_withoutCorrelationId_generatesUuid() {
        AgentMessageDTO dto =
                new AgentMessageDTO("agent", "run command");

        assertEquals("agent", dto.getSender());
        assertEquals("run command", dto.getContent());
        assertNotNull(dto.getCorrelationId());

        // UUID format sanity check
        assertDoesNotThrow(() ->
                java.util.UUID.fromString(dto.getCorrelationId()));
    }
}
