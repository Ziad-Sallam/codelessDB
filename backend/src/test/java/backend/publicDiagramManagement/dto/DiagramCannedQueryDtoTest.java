package backend.publicDiagramManagement.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import backend.entities.publicDiagramEntities.CannedQueriesDiagrams;

class DiagramCannedQueryDtoTest {

    @Test
    void testToDto() {
        CannedQueriesDiagrams entity = new CannedQueriesDiagrams();
        entity.setName("Test Query");
        entity.setDescription("Test Description");
        entity.setQuery("SELECT * FROM test");

        DiagramCannedQueryDto dto = DiagramCannedQueryDto.toDto(entity);

        assertEquals("Test Query", dto.getName());
        assertEquals("Test Description", dto.getDescription());
        assertEquals("SELECT * FROM test", dto.getQuery());
    }

    @Test
    void testNoArgsConstructorAndSetters() {
        DiagramCannedQueryDto dto = new DiagramCannedQueryDto();
        dto.setName("Name");
        dto.setDescription("Desc");
        dto.setQuery("SQL");

        assertEquals("Name", dto.getName());
        assertEquals("Desc", dto.getDescription());
        assertEquals("SQL", dto.getQuery());
    }

    @Test
    void testAllArgsConstructorAndBuilder() {
        DiagramCannedQueryDto dto = new DiagramCannedQueryDto("Name", "Desc", "SQL", 1);
        assertEquals("Name", dto.getName());

        DiagramCannedQueryDto builderDto = DiagramCannedQueryDto.builder()
                .name("BName")
                .description("BDesc")
                .query("BSQL")
                .build();
        
        assertEquals("BName", builderDto.getName());
        assertEquals("BDesc", builderDto.getDescription());
        assertEquals("BSQL", builderDto.getQuery());
    }

    @Test
    void testEqualsAndHashCode() {
        DiagramCannedQueryDto dto1 = new DiagramCannedQueryDto("A", "B", "C", 1);
        DiagramCannedQueryDto dto2 = new DiagramCannedQueryDto("A", "B", "C", 1);
        DiagramCannedQueryDto dto3 = new DiagramCannedQueryDto("X", "Y", "Z", 1);

        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
        
        assertNotEquals(null, dto1);
        assertNotEquals(dto1, new Object());
    }

    @Test
    void testToString() {
        DiagramCannedQueryDto dto = new DiagramCannedQueryDto("A", "B", "C", 1);
        String str = dto.toString();
        assertTrue(str.contains("name=A"));
        assertTrue(str.contains("description=B"));
        assertTrue(str.contains("query=C"));
    }
}
