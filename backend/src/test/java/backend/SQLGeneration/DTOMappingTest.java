package backend.SQLGeneration;

import backend.SQLGeneration.dto.*;
import backend.SQLGeneration.dto.constraint.*;
import backend.SQLGeneration.dto.SQLTypeName;
import backend.SQLGeneration.dto.constraint.Action;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DTOMappingTest {

    private static SchemaDTO schema;

    @BeforeAll
    static void setup() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        URL resource = DTOMappingTest.class.getClassLoader().getResource("schema.json");
        assertNotNull(resource, "JSON file must exist in src/test/resources/schema.json");
        schema = mapper.readValue(resource, SchemaDTO.class);
        assertNotNull(schema, "SchemaDTO should not be null");
        assertNotNull(schema.getEntities(), "Entities list should not be null");
    }

    @Test
    void testEntitiesExist() {
        assertEquals(2, schema.getEntities().size(), "Schema should contain 2 entities");

        assertEntityExists("Employee");
        assertEntityExists("Department");
    }

    @Test
    void testEmployeeEntity() {
        EntityDTO employee = getEntity("Employee");
        assertEquals(4, employee.getAttributes().size(), "Employee should have 4 attributes");

        assertAttribute(employee, "id", SQLTypeName.INT, true,
                ConstraintType.PRIMARY_KEY,
                ConstraintType.NOT_NULL);

        assertAttribute(employee, "name", SQLTypeName.VARCHAR, false,
                ConstraintType.NOT_NULL);

        assertAttribute(employee, "salary", SQLTypeName.DECIMAL, false,
                ConstraintType.CHECK,
                ConstraintType.DEFAULT);

        assertCheckConstraint(employee, "salary", "salary > 0");
        assertDefaultConstraint(employee, "salary", "1000");

        assertForeignKey(employee, "department_id", "Department", "id", Action.CASCADE, Action.NO_ACTION);
    }

    @Test
    void testDepartmentEntity() {
        EntityDTO dept = getEntity("Department");
        assertEquals(2, dept.getAttributes().size(), "Department should have 2 attributes");

        assertAttribute(dept, "id", SQLTypeName.INT, true, ConstraintType.PRIMARY_KEY);
        assertAttribute(dept, "name", SQLTypeName.VARCHAR, false, ConstraintType.NOT_NULL);
    }

    // ---------------- Helper Methods ----------------

    private void assertEntityExists(String name) {
        assertTrue(schema.getEntities().stream().anyMatch(e -> name.equals(e.getName())),
                "Entity '" + name + "' should exist");
    }

    private EntityDTO getEntity(String name) {
        return schema.getEntities().stream()
                .filter(e -> name.equals(e.getName()))
                .findFirst().orElseThrow(() -> new AssertionError("Entity '" + name + "' not found"));
    }

    private void assertAttribute(EntityDTO entity, String attrName, SQLTypeName type,
                                 boolean indexed, ConstraintType... constraints) {
        AttributeDTO attr = entity.getAttributes().stream()
                .filter(a -> attrName.equals(a.getName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Attribute '" + attrName + "' not found in entity " + entity.getName()));

        assertEquals(type, attr.getDataType().getName(), "Attribute '" + attrName + "' data type mismatch");
        assertEquals(indexed, attr.isIndexed(), "Attribute '" + attrName + "' indexed flag mismatch");

        for (ConstraintType ct : constraints) {
            assertTrue(attr.getConstraints().stream().anyMatch(c -> c.getType() == ct),
                    "Attribute '" + attrName + "' should have constraint: " + ct);
        }
    }

    private void assertCheckConstraint(EntityDTO entity, String attrName, String expression) {
        AttributeDTO attr = getAttribute(entity, attrName);
        boolean found = attr.getConstraints().stream()
                .filter(c -> c instanceof CheckConstraint)
                .map(c -> (CheckConstraint) c)
                .anyMatch(c -> expression.equals(c.getExpression()));
        assertTrue(found, "CheckConstraint with expression '" + expression + "' not found on attribute '" + attrName + "'");
    }

    private void assertDefaultConstraint(EntityDTO entity, String attrName, String defaultValue) {
        AttributeDTO attr = getAttribute(entity, attrName);
        boolean found = attr.getConstraints().stream()
                .filter(c -> c instanceof DefaultConstraintDTO)
                .map(c -> (DefaultConstraintDTO) c)
                .anyMatch(c -> defaultValue.equals(c.getDefaultValue()));
        assertTrue(found, "DefaultConstraint with value '" + defaultValue + "' not found on attribute '" + attrName + "'");
    }

    private void assertForeignKey(EntityDTO entity, String attrName, String refTable,
                                  String refColumn, Action onDelete, Action onUpdate) {
        AttributeDTO attr = getAttribute(entity, attrName);
        boolean found = attr.getConstraints().stream()
                .filter(c -> c instanceof ForeignKeyConstraintDTO)
                .map(c -> (ForeignKeyConstraintDTO) c)
                .anyMatch(c -> refTable.equals(c.getReferencedTable()) &&
                        refColumn.equals(c.getReferencedColumn()) &&
                        c.getOnDelete() == onDelete &&
                        c.getOnUpdate() == onUpdate);
        assertTrue(found, "ForeignKeyConstraint on attribute '" + attrName + "' not matching expected values");
    }

    private AttributeDTO getAttribute(EntityDTO entity, String attrName) {
        return entity.getAttributes().stream()
                .filter(a -> attrName.equals(a.getName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Attribute '" + attrName + "' not found in entity " + entity.getName()));
    }
}
