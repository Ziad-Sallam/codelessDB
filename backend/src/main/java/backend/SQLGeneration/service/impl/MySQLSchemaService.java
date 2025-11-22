
package backend.SQLGeneration.service.impl;

import backend.SQLGeneration.dto.*;
import backend.SQLGeneration.dto.constraint.*;
import backend.SQLGeneration.service.SchemaService;
import backend.SQLGeneration.service.util.SchemaValidationException;
import backend.SQLGeneration.service.util.SchemaValidatorService;
import backend.SQLGeneration.service.util.TopoSortService;
import org.hibernate.tool.schema.spi.SchemaValidator;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MySQLSchemaService implements SchemaService {

    private final TopoSortService sorter = new TopoSortService();
    private final SchemaValidatorService schemaValidator = new SchemaValidatorService();
    @Override
    public String generateDDL(SchemaDTO schemaDTO) {
        if (schemaDTO == null) throw new SchemaValidationException("SchemaDTO is null");
        if (schemaDTO.getEntities() == null || schemaDTO.getEntities().isEmpty())
            throw new SchemaValidationException("Schema has no entities");

        schemaValidator.validateSchema(schemaDTO); // <-- new validation

        StringBuilder ddl = new StringBuilder();
        String schemaName = schemaDTO.getSchemaName();

        if (schemaName != null && !schemaName.isBlank()) {
            ddl.append("CREATE DATABASE IF NOT EXISTS ").append(schemaName).append(";\n");
            ddl.append("USE ").append(schemaName).append(";\n\n");
        }

        List<EntityDTO> sorted = sorter.sortEntitiesByDependencies(schemaDTO.getEntities());

        for (EntityDTO e : sorted) {
            ddl.append(generateTableDDL(e)).append("\n\n");
        }

        return ddl.toString();
    }


    private String generateTableDDL(EntityDTO entity) {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE ").append(entity.getName()).append(" (\n");

        List<AttributeDTO> attributes = entity.getAttributes();
        if (attributes == null) attributes = new ArrayList<>();

        List<String> columnDefs = new ArrayList<>();
        List<String> primaryKeys = new ArrayList<>();
        Map<String, List<ForeignKeyConstraintDTO>> fksByTable = new LinkedHashMap<>();
        Map<String, List<String>> referencingColumns = new LinkedHashMap<>();

        for (AttributeDTO attr : attributes) {
            columnDefs.add("\t" + generateColumnDefinition(attr));

            if (attr.getConstraints() != null) {
                for (ConstraintDTO c : attr.getConstraints()) {
                    if (c instanceof PrimaryKeyConstraintDTO) primaryKeys.add(attr.getName());
                    if (c instanceof ForeignKeyConstraintDTO fk) {
                        fksByTable.computeIfAbsent(fk.getReferencedTable(), k -> new ArrayList<>()).add(fk);
                        referencingColumns.computeIfAbsent(fk.getReferencedTable(), k -> new ArrayList<>()).add(attr.getName());
                    }
                }
            }
        }

        sb.append(String.join(",\n", columnDefs));

        if (!primaryKeys.isEmpty()) {
            sb.append(",\n\tPRIMARY KEY (").append(String.join(", ", primaryKeys)).append(")");
        }

        for (String refTable : fksByTable.keySet()) {
            List<ForeignKeyConstraintDTO> fkList = fksByTable.get(refTable);
            List<String> referencingCols = referencingColumns.get(refTable);
            for (int i = 0; i < fkList.size(); i++) {
                sb.append(",\n\t").append(fkList.get(i).toSQL(referencingCols.get(i)));
            }
        }

        sb.append("\n);");

        for (AttributeDTO attr : attributes) {
//            boolean isPk = false;
//            for (ConstraintDTO c : attr.getConstraints()) {
//                if (c instanceof PrimaryKeyConstraintDTO) {
//                    isPk = true;
//                    break;
//                }
//            }
//            if (isPk)
//                continue;
            if (attr.isIndexed()) {
                sb.append("\nCREATE INDEX idx_")
                        .append(entity.getName()).append("_").append(attr.getName())
                        .append(" ON ").append(entity.getName())
                        .append("(").append(attr.getName()).append(");");
            }
        }

        return sb.toString();
    }

    private String generateColumnDefinition(AttributeDTO attr) {
        StringBuilder sb = new StringBuilder();
        String ddlType = attr.getDataType().toDDL();
        sb.append(attr.getName()).append(" ").append(ddlType);

        if (attr.getConstraints() != null) {
            for (ConstraintDTO c : attr.getConstraints()) {
                if (c instanceof PrimaryKeyConstraintDTO) continue;
                if (c instanceof ForeignKeyConstraintDTO) continue;
                sb.append(" ").append(c.toSQL());
            }
        }

        if (attr.isAutoIncrement()) sb.append(" AUTO_INCREMENT");
        return sb.toString();
    }
}
