package backend.SQLGeneration.service.util;

import backend.SQLGeneration.dto.*;
import backend.SQLGeneration.dto.constraint.*;

import java.util.*;

public class SchemaValidatorService {

    /**
     * Validates the given schema for:
     * 1. Null or empty schema/entities
     * 2. Duplicate entity names
     * 3. Foreign key references (table + column)
     * Throws SchemaValidationException if invalid
     */
    public void validateSchema(SchemaDTO schema) {
        if (schema == null) throw new SchemaValidationException("SchemaDTO is null");

        List<EntityDTO> entities = schema.getEntities();
        if (entities == null || entities.isEmpty())
            throw new SchemaValidationException("Schema has no entities");

        Map<String, EntityDTO> entityMap = buildEntityMap(entities);

        entities.forEach(entity -> validateEntity(entity, entityMap));
    }

    /**
     * Builds a map of entity name → EntityDTO
     * Checks for duplicate entity names
     */
    private Map<String, EntityDTO> buildEntityMap(List<EntityDTO> entities) {
        Map<String, EntityDTO> entityMap = new HashMap<>();
        for (EntityDTO e : entities) {
            String name = e.getName();
            if (entityMap.containsKey(name)) {
                throw new SchemaValidationException("Duplicate entity: " + name);
            }
            entityMap.put(name, e);
        }
        return entityMap;
    }

    /**
     * Validates all attributes of an entity
     */
    private void validateEntity(EntityDTO entity, Map<String, EntityDTO> entityMap) {
        if (entity.getAttributes() == null) return;

        entity.getAttributes().forEach(attr -> validateAttribute(attr, entity, entityMap));
    }

    /**
     * Validates constraints for a single attribute
     */
    private void validateAttribute(AttributeDTO attr, EntityDTO entity, Map<String, EntityDTO> entityMap) {
        if (attr.getConstraints() == null) return;

        for (ConstraintDTO cons : attr.getConstraints()) {
            if (cons instanceof ForeignKeyConstraintDTO fk) {
                validateForeignKey(fk, attr, entity, entityMap);
            }
        }
    }

    /**
     * Validates a foreign key:
     * - referenced table exists
     * - referenced column exists in that table
     */
    private void validateForeignKey(ForeignKeyConstraintDTO fk, AttributeDTO attr, EntityDTO entity, Map<String, EntityDTO> entityMap) {
        String refTable = fk.getReferencedTable();
        if (refTable == null || !entityMap.containsKey(refTable)) {
            throw new SchemaValidationException(
                    String.format("Invalid FK in '%s.%s': referenced table '%s' does not exist.",
                            entity.getName(), attr.getName(), refTable)
            );
        }

        String refColumn = fk.getReferencedColumn();
        if (!columnExists(refColumn, entityMap.get(refTable))) {
            throw new SchemaValidationException(
                    String.format("Invalid FK in '%s.%s': referenced column '%s' does not exist in table '%s'.",
                            entity.getName(), attr.getName(), refColumn, refTable)
            );
        }
    }

    /**
     * Checks if a column exists in the given entity
     */
    private boolean columnExists(String columnName, EntityDTO entity) {
        if (entity.getAttributes() == null) return false;
        return entity.getAttributes().stream()
                .anyMatch(attr -> attr.getName().equals(columnName));
    }
}