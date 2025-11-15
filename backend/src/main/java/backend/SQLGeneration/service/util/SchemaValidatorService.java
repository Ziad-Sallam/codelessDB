package backend.SQLGeneration.service.util;

import backend.SQLGeneration.dto.*;
import backend.SQLGeneration.dto.constraint.ForeignKeyConstraintDTO;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class SchemaValidatorService {
    public void validateSchema(SchemaDTO schemaDTO) {
        Map<String, EntityDTO> entityMap = new HashMap<>();

        for (EntityDTO entity : schemaDTO.getEntities()) {
            if (entity.getName() == null || entity.getName().isBlank()) {
                throw new SchemaValidationException("Entity with null or blank name found");
            }
            if (entityMap.containsKey(entity.getName())) {
                throw new SchemaValidationException("Duplicate entity name: " + entity.getName());
            }
            entityMap.put(entity.getName(), entity);

            Set<String> attrNames = new HashSet<>();
            if (entity.getAttributes() != null) {
                for (AttributeDTO attr : entity.getAttributes()) {
                    if (attr.getName() == null || attr.getName().isBlank()) {
                        throw new SchemaValidationException("Attribute with null or blank name in entity: " + entity.getName());
                    }
                    if (!attrNames.add(attr.getName())) {
                        throw new SchemaValidationException("Duplicate attribute name '" + attr.getName() + "' in entity: " + entity.getName());
                    }

                    // Check foreign keys
                    if (attr.getConstraints() != null) {
                        for (ConstraintDTO c : attr.getConstraints()) {
                            if (c instanceof ForeignKeyConstraintDTO fk) {
                                String refTable = fk.getReferencedTable();
                                String refColumn = fk.getReferencedColumn();

                                if (!entityMap.containsKey(refTable)) {
                                    throw new SchemaValidationException(
                                            "Invalid FK in " + entity.getName() + "." + attr.getName() +
                                                    ": referenced table '" + refTable + "' does not exist"
                                    );
                                }

                                EntityDTO referenced = entityMap.get(refTable);
                                boolean columnExists = referenced.getAttributes().stream()
                                        .anyMatch(a -> a.getName().equals(refColumn));
                                if (!columnExists) {
                                    throw new SchemaValidationException(
                                            "Invalid FK in " + entity.getName() + "." + attr.getName() +
                                                    ": referenced column '" + refColumn + "' does not exist in table " + refTable
                                    );
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}
