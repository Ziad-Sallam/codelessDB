package backend.SQLGeneration.service.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import backend.SQLGeneration.dto.EntityDTO;
import backend.SQLGeneration.dto.SchemaDTO;
import backend.SQLGeneration.service.util.SchemaValidationException;

class MySQLSchemaServiceTest {

    private MySQLSchemaService schemaService;

    @BeforeEach
    void setUp() {
        schemaService = new MySQLSchemaService();
    }

    @Test
    void generateDDL_nullSchema_throwsException() {
        SchemaValidationException ex = assertThrows(SchemaValidationException.class, () -> schemaService.generateDDL(null));
        assertEquals("SchemaDTO is null", ex.getMessage());
    }

    @Test
    void generateDDL_nullEntities_throwsException() {
        SchemaDTO schema = new SchemaDTO("test", null);
        SchemaValidationException ex = assertThrows(SchemaValidationException.class, () -> schemaService.generateDDL(schema));
        assertEquals("Schema has no entities", ex.getMessage());
    }

    @Test
    void generateDDL_emptyEntities_throwsException() {
        SchemaDTO schema = new SchemaDTO("test", Collections.emptyList());
        SchemaValidationException ex = assertThrows(SchemaValidationException.class, () -> schemaService.generateDDL(schema));
        assertEquals("Schema has no entities", ex.getMessage());
    }

    @Test
    void generateDDL_withDatabaseName_success() {
        EntityDTO entity = new EntityDTO("User", new ArrayList<>());
        SchemaDTO schema = new SchemaDTO("my_db", Collections.singletonList(entity));

        String ddl = schemaService.generateDDL(schema);

        assertTrue(ddl.contains("CREATE DATABASE IF NOT EXISTS my_db;"));
        assertTrue(ddl.contains("USE my_db;"));
        assertTrue(ddl.contains("CREATE TABLE User"));
    }

    @Test
    void generateDDL_withoutDatabaseName_success() {
        EntityDTO entity = new EntityDTO("User", new ArrayList<>());
        SchemaDTO schema = new SchemaDTO(null, Collections.singletonList(entity));

        String ddl = schemaService.generateDDL(schema);

        assertFalse(ddl.contains("CREATE DATABASE"));
        assertTrue(ddl.contains("CREATE TABLE User"));
    }
}
