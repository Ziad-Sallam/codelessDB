package backend.SQLGeneration;

import backend.SQLGeneration.dto.*;
import backend.SQLGeneration.dto.constraint.*;
import backend.SQLGeneration.dto.SQLTypeName;
import backend.SQLGeneration.dto.constraint.ForeignKeyAction;
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
    }

    // ----------- Schema Level Tests -----------

    @Test
    void schemaShouldHaveName() {
        assertNotNull(schema.getSchemaName(), "Schema should have a name");
    }

    @Test
    void schemaShouldHaveEntities() {
        assertFalse(schema.getEntities().isEmpty(), "Schema should have at least one entity");
    }

    @Test
    void entityNamesShouldExist() {
        assertEntityExists("Employee");
        assertEntityExists("Department");
        assertEntityExists("Project"); // optional
    }

    // ----------- Employee Tests -----------

    @Test
    void employeeShouldHaveFiveAttributes() {
        assertEquals(5, getEntity("Employee").getAttributes().size(), "Employee should have 5 attributes");
    }

    @SuppressWarnings("unchecked")
    @Test
    void employeeIdAttribute() {
        assertAttribute("Employee", "id", SQLTypeName.INT, true, PrimaryKeyConstraintDTO.class, NotNullConstraintDTO.class);
    }

    @SuppressWarnings("unchecked")
    @Test
    void employeeSalaryAttribute() {
        assertAttribute("Employee", "salary", SQLTypeName.DECIMAL, false, CheckConstraintDTO.class, DefaultConstraintDTO.class);
        assertCheckConstraint("Employee", "salary", "salary > 0");
        assertDefaultConstraint("Employee", "salary", "1000");
    }

    @SuppressWarnings("unchecked")
    @Test
    void employeeDepartmentFk() {
        assertAttribute("Employee", "department_id", SQLTypeName.INT, false, ForeignKeyConstraintDTO.class);
        assertForeignKey("Employee", "department_id", "Department", "id", ForeignKeyAction.CASCADE, ForeignKeyAction.NO_ACTION);
    }

    // ----------- Department Tests -----------

    @SuppressWarnings("unchecked")
    @Test
    void departmentIdAndName() {
        assertAttribute("Department", "id", SQLTypeName.INT, true, PrimaryKeyConstraintDTO.class);
        assertAttribute("Department", "name", SQLTypeName.VARCHAR, false, NotNullConstraintDTO.class);
    }

    // ----------- Project Tests -----------

    @Test
    void projectShouldHaveEnumAndSet() {
        EntityDTO project = getEntity("Project");
        assertTrue(project.getAttributes().stream().anyMatch(a -> a.getDataType().getName() == SQLTypeName.ENUM), "Project should have ENUM attribute");
        assertTrue(project.getAttributes().stream().anyMatch(a -> a.getDataType().getName() == SQLTypeName.SET), "Project should have SET attribute");
    }

    @SuppressWarnings("unchecked")
    @Test
    void projectIdNameStatusTags() {
        assertAttribute("Project", "id", SQLTypeName.INT, true, PrimaryKeyConstraintDTO.class);
        assertAttribute("Project", "name", SQLTypeName.VARCHAR, false, NotNullConstraintDTO.class);
        assertAttribute("Project", "status", SQLTypeName.ENUM, false);
        assertAttribute("Project", "tags", SQLTypeName.SET, false);
    }

    // ----------- Helper Methods -----------

    private void assertEntityExists(String name) {
        assertTrue(schema.getEntities().stream().anyMatch(e -> name.equals(e.getName())),
                "Entity '" + name + "' should exist");
    }

    private EntityDTO getEntity(String name) {
        return schema.getEntities().stream()
                .filter(e -> name.equals(e.getName()))
                .findFirst().orElseThrow(() -> new AssertionError("Entity '" + name + "' not found"));
    }

    private AttributeDTO getAttribute(EntityDTO entity, String attrName) {
        return entity.getAttributes().stream()
                .filter(a -> attrName.equals(a.getName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Attribute '" + attrName + "' not found in entity " + entity.getName()));
    }

    private void assertAttribute(String entityName, String attrName, SQLTypeName type, boolean indexed,
                                 @SuppressWarnings("unchecked")  Class<? extends ConstraintDTO>... constraintClasses) {
        AttributeDTO attr = getAttribute(getEntity(entityName), attrName);
        assertEquals(type, attr.getDataType().getName(), "Attribute '" + attrName + "' data type mismatch");
        assertEquals(indexed, attr.isIndexed(), "Attribute '" + attrName + "' indexed flag mismatch");
        for (Class<? extends ConstraintDTO> clazz : constraintClasses) {
            assertTrue(attr.getConstraints().stream().anyMatch(c -> clazz.isInstance(c)),
                    "Attribute '" + attrName + "' should have constraint: " + clazz.getSimpleName());
        }
    }

    private void assertCheckConstraint(String entityName, String attrName, String expression) {
        AttributeDTO attr = getAttribute(getEntity(entityName), attrName);
        boolean found = attr.getConstraints().stream()
                .filter(c -> c instanceof CheckConstraintDTO)
                .map(c -> (CheckConstraintDTO) c)
                .anyMatch(c -> expression.equals(c.getExpression()));
        assertTrue(found, "CheckConstraint with expression '" + expression + "' not found on attribute '" + attrName + "'");
    }

    private void assertDefaultConstraint(String entityName, String attrName, String defaultValue) {
        AttributeDTO attr = getAttribute(getEntity(entityName), attrName);
        boolean found = attr.getConstraints().stream()
                .filter(c -> c instanceof DefaultConstraintDTO)
                .map(c -> (DefaultConstraintDTO) c)
                .anyMatch(c -> defaultValue.equals(c.getDefaultValue()));
        assertTrue(found, "DefaultConstraint with value '" + defaultValue + "' not found on attribute '" + attrName + "'");
    }

    private void assertForeignKey(String entityName, String attrName, String refTable,
                                  String refColumn, ForeignKeyAction onDelete, ForeignKeyAction onUpdate) {
        AttributeDTO attr = getAttribute(getEntity(entityName), attrName);
        boolean found = attr.getConstraints().stream()
                .filter(c -> c instanceof ForeignKeyConstraintDTO)
                .map(c -> (ForeignKeyConstraintDTO) c)
                .anyMatch(c -> refTable.equals(c.getReferencedTable()) &&
                        refColumn.equals(c.getReferencedColumn()) &&
                        c.getOnDelete() == onDelete &&
                        c.getOnUpdate() == onUpdate);
        assertTrue(found, "ForeignKeyConstraint on attribute '" + attrName + "' not matching expected values");
    }

    @Test
    void checkConstraintDTO_gettersSetters() {
        CheckConstraintDTO check = new CheckConstraintDTO("x > 0");
        assertEquals("x > 0", check.getExpression());
    }

    @Test
    void defaultConstraintDTO_gettersSetters() {
        DefaultConstraintDTO def = new DefaultConstraintDTO("100");
        assertEquals("100", def.getDefaultValue());
    }

    @Test
    void foreignKeyConstraintDTO_gettersSetters() {
        ForeignKeyConstraintDTO fk = new ForeignKeyConstraintDTO(
                "Employee", "id", ForeignKeyAction.CASCADE, ForeignKeyAction.NO_ACTION
        );
        assertEquals("Employee", fk.getReferencedTable());
        assertEquals("id", fk.getReferencedColumn());
        assertEquals(ForeignKeyAction.CASCADE, fk.getOnDelete());
        assertEquals(ForeignKeyAction.NO_ACTION, fk.getOnUpdate());
    }

    @Test
    void primaryKeyConstraintDTO_test() {
        PrimaryKeyConstraintDTO pk = new PrimaryKeyConstraintDTO();
        assertNotNull(pk);
    }

    @Test
    void sqlTypeNameEnumTest() {
        for (SQLTypeName name : SQLTypeName.values()) {
            assertNotNull(name.name());
        }
    }

    @Test
    void sqlDataTypeTest() {
        SQLDataType intType = new SQLDataType(SQLTypeName.INT, null, null, null, null);
        assertEquals(SQLTypeName.INT, intType.getName());
        SQLDataType varcharType = new SQLDataType(SQLTypeName.VARCHAR, 100, null, null, null);
        assertEquals(100, varcharType.getLength());
    }

    @Test
    void foreignKeyActionEnumTest() {
        for (ForeignKeyAction action : ForeignKeyAction.values()) {
            assertNotNull(action.name());
        }
    }

    @Test
    void checkConstraintEqualsHash() {
        CheckConstraintDTO c1 = new CheckConstraintDTO("x>0");
        CheckConstraintDTO c2 = new CheckConstraintDTO("x>0");
        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
    }
}
