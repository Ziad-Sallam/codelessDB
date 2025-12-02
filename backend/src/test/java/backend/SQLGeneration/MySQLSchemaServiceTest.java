package backend.SQLGeneration;

import backend.SQLGeneration.dto.*;
import backend.SQLGeneration.service.impl.MySQLSchemaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

public class MySQLSchemaServiceTest {

    private static String ddl;

    @BeforeAll
    static void setup() throws Exception {
        // Load JSON
        ObjectMapper mapper = new ObjectMapper();
        URL resource = MySQLSchemaServiceTest.class.getClassLoader().getResource("schema.json");
        assertNotNull(resource, "JSON file must exist in src/test/resources/schema.json");
        SchemaDTO schema = mapper.readValue(resource, SchemaDTO.class);
        assertNotNull(schema, "SchemaDTO should not be null");

        // Generate DDL
        MySQLSchemaService service = new MySQLSchemaService();
        ddl = service.generateDDL(schema);
        assertNotNull(ddl, "Generated DDL should not be null");
    }

    @Test
    void testDatabaseCreation() {
        assertTrue(ddl.contains("CREATE DATABASE IF NOT EXISTS TestDB"));
        assertTrue(ddl.contains("USE TestDB"));
    }

    @Test
    void testTablesExist() {
        assertTrue(ddl.contains("CREATE TABLE Employee"));
        assertTrue(ddl.contains("CREATE TABLE Department"));
        assertTrue(ddl.contains("CREATE TABLE Project"));
    }

    @Test
    void testTableOrder() {
        assertTrue(ddl.indexOf("CREATE TABLE Department") < ddl.indexOf("CREATE TABLE Employee"),
                "Department must be before Employee due to FK");
    }

    @Test
    void testDataTypes() {
        assertTrue(ddl.contains("VARCHAR(100)"));
        assertTrue(ddl.contains("VARCHAR(200)"));
        assertTrue(ddl.contains("DECIMAL(10,2)"));
        assertTrue(ddl.contains("FLOAT"));
        assertTrue(ddl.contains("ENUM('active','inactive','completed')"));
        assertTrue(ddl.contains("SET('urgent','internal','external')"));
    }

    @Test
    void testConstraints() {
        assertTrue(ddl.contains("PRIMARY KEY"));
        assertTrue(ddl.contains("NOT NULL"));
        assertTrue(ddl.contains("UNIQUE"));
        assertTrue(ddl.contains("DEFAULT 1000") || ddl.contains("DEFAULT 'active'"));
        assertTrue(ddl.contains("CHECK (salary > 0)"));
        assertTrue(ddl.contains("CHECK (bonus >= 0)"));
    }

    @Test
    void testForeignKeys() {
        assertTrue(ddl.contains("FOREIGN KEY (department_id) REFERENCES Department(id)"));
        assertTrue(ddl.contains("ON DELETE CASCADE"));
        assertTrue(ddl.contains("ON UPDATE NO ACTION"));
    }

    @Test
    void testIndexes() {
        assertTrue(ddl.contains("CREATE INDEX idx_Employee_id ON Employee(id)"));
        assertTrue(ddl.contains("CREATE INDEX idx_Department_id ON Department(id)"));
        assertTrue(ddl.contains("CREATE INDEX idx_Project_id ON Project(id)"));
    }

    @Test
    void testSyntaxSanity() {
        assertFalse(ddl.contains(" ,)"));
        assertFalse(ddl.contains(",,"), "No double commas allowed");
        assertTrue(ddl.trim().endsWith(";"));
    }
}
