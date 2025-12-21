package backend.SQLGeneration.service.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import backend.SQLGeneration.dto.AttributeDTO;
import backend.SQLGeneration.dto.EntityDTO;
import backend.SQLGeneration.dto.SchemaDTO;
import backend.SQLGeneration.dto.constraint.ForeignKeyConstraintDTO;

class SchemaValidatorServiceTest {

    private SchemaValidatorService validatorService;

    @BeforeEach
    void setUp() {
        validatorService = new SchemaValidatorService();
    }

    @Test
    void validateSchema_nullSchema_throwsException() {
        SchemaValidationException ex = assertThrows(SchemaValidationException.class, () -> validatorService.validateSchema(null));
        assertEquals("SchemaDTO is null", ex.getMessage());
    }

    @Test
    void validateSchema_nullEntities_throwsException() {
        SchemaDTO schema = new SchemaDTO("test", null);
        SchemaValidationException ex = assertThrows(SchemaValidationException.class, () -> validatorService.validateSchema(schema));
        assertEquals("Schema has no entities", ex.getMessage());
    }

    @Test
    void validateSchema_emptyEntities_throwsException() {
        SchemaDTO schema = new SchemaDTO("test", Collections.emptyList());
        SchemaValidationException ex = assertThrows(SchemaValidationException.class, () -> validatorService.validateSchema(schema));
        assertEquals("Schema has no entities", ex.getMessage());
    }

    @Test
    void validateSchema_duplicateEntityNames_throwsException() {
        EntityDTO e1 = new EntityDTO("User", new ArrayList<>());
        EntityDTO e2 = new EntityDTO("User", new ArrayList<>());
        SchemaDTO schema = new SchemaDTO("test", Arrays.asList(e1, e2));

        SchemaValidationException ex = assertThrows(SchemaValidationException.class, () -> validatorService.validateSchema(schema));
        assertTrue(ex.getMessage().contains("Duplicate entity: User"));
    }

    @Test
    void validateSchema_validFK_success() {
        AttributeDTO refAttr = new AttributeDTO("id", null, false, null, false);
        EntityDTO target = new EntityDTO("Role", Collections.singletonList(refAttr));

        ForeignKeyConstraintDTO fk = new ForeignKeyConstraintDTO("Role", "id", null, null);
        AttributeDTO fkAttr = new AttributeDTO("role_id", null, false, Collections.singletonList(fk), false);
        EntityDTO source = new EntityDTO("User", Collections.singletonList(fkAttr));

        SchemaDTO schema = new SchemaDTO("test", Arrays.asList(target, source));

        assertDoesNotThrow(() -> validatorService.validateSchema(schema));
    }

    @Test
    void validateSchema_invalidFKTable_throwsException() {
        ForeignKeyConstraintDTO fk = new ForeignKeyConstraintDTO("NonExistent", "id", null, null);
        AttributeDTO fkAttr = new AttributeDTO("role_id", null, false, Collections.singletonList(fk), false);
        EntityDTO source = new EntityDTO("User", Collections.singletonList(fkAttr));

        SchemaDTO schema = new SchemaDTO("test", Collections.singletonList(source));

        SchemaValidationException ex = assertThrows(SchemaValidationException.class, () -> validatorService.validateSchema(schema));
        assertTrue(ex.getMessage().contains("referenced table 'NonExistent' does not exist"));
    }

    @Test
    void validateSchema_invalidFKColumn_throwsException() {
        EntityDTO target = new EntityDTO("Role", new ArrayList<>());

        ForeignKeyConstraintDTO fk = new ForeignKeyConstraintDTO("Role", "missing_id", null, null);
        AttributeDTO fkAttr = new AttributeDTO("role_id", null, false, Collections.singletonList(fk), false);
        EntityDTO source = new EntityDTO("User", Collections.singletonList(fkAttr));

        SchemaDTO schema = new SchemaDTO("test", Arrays.asList(target, source));

        SchemaValidationException ex = assertThrows(SchemaValidationException.class, () -> validatorService.validateSchema(schema));
        assertTrue(ex.getMessage().contains("referenced column 'missing_id' does not exist in table 'Role'"));
    }

    @Test
    void validateSchema_nullAttributesInFKTable_throwsException() {
        EntityDTO target = new EntityDTO("Role", null);

        ForeignKeyConstraintDTO fk = new ForeignKeyConstraintDTO("Role", "id", null, null);
        AttributeDTO fkAttr = new AttributeDTO("role_id", null, false, Collections.singletonList(fk), false);
        EntityDTO source = new EntityDTO("User", Collections.singletonList(fkAttr));

        SchemaDTO schema = new SchemaDTO("test", Arrays.asList(target, source));

        SchemaValidationException ex = assertThrows(SchemaValidationException.class, () -> validatorService.validateSchema(schema));
        assertTrue(ex.getMessage().contains("referenced column 'id' does not exist in table 'Role'"));
    }
}
