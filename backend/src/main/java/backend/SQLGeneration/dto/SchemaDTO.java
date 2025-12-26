package backend.SQLGeneration.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    description = "Represents a complete database schema structure",
    example = """
        {
          "schemaName": "TestDB",
          "entities": [
            {
              "name": "Employee",
              "attributes": [
                {
                  "name": "id",
                  "dataType": { "name": "INT" },
                  "indexed": true,
                  "autoIncrement": true,
                  "constraints": [
                    { "type": "PRIMARY_KEY" },
                    { "type": "NOT_NULL" }
                  ]
                },
                {
                  "name": "name",
                  "dataType": { "name": "VARCHAR", "length": 100 },
                  "indexed": false,
                  "constraints": [
                    { "type": "NOT_NULL" }
                  ]
                },
                {
                  "name": "salary",
                  "dataType": { "name": "DECIMAL", "precision": 10, "scale": 2 },
                  "indexed": false,
                  "constraints": [
                    { "type": "CHECK", "expression": "salary > 0" },
                    { "type": "DEFAULT", "defaultValue": "1000" }
                  ]
                },
                {
                  "name": "department_id",
                  "dataType": { "name": "INT" },
                  "indexed": false,
                  "constraints": [
                    {
                      "type": "FOREIGN_KEY",
                      "referencedTable": "Department",
                      "referencedColumn": "id",
                      "onDelete": "CASCADE",
                      "onUpdate": "NO_ACTION"
                    }
                  ]
                },
                {
                  "name": "bonus",
                  "dataType": { "name": "FLOAT" },
                  "indexed": false,
                  "constraints": [
                    { "type": "CHECK", "expression": "bonus >= 0" }
                  ]
                }
              ]
            },
            {
              "name": "Department",
              "attributes": [
                {
                  "name": "id",
                  "dataType": { "name": "INT" },
                  "indexed": true,
                  "autoIncrement": true,
                  "constraints": [
                    { "type": "PRIMARY_KEY" }
                  ]
                },
                {
                  "name": "name",
                  "dataType": { "name": "VARCHAR", "length": 100 },
                  "indexed": false,
                  "constraints": [
                    { "type": "NOT_NULL" },
                    { "type": "UNIQUE" }
                  ]
                }
              ]
            },
            {
              "name": "Project",
              "attributes": [
                {
                  "name": "id",
                  "dataType": { "name": "INT" },
                  "indexed": true,
                  "autoIncrement": true,
                  "constraints": [
                    { "type": "PRIMARY_KEY" }
                  ]
                },
                {
                  "name": "name",
                  "dataType": { "name": "VARCHAR", "length": 200 },
                  "indexed": false,
                  "constraints": [
                    { "type": "NOT_NULL" }
                  ]
                },
                {
                  "name": "status",
                  "dataType": { "name": "ENUM", "values": ["active","inactive","completed"] },
                  "indexed": false,
                  "constraints": [
                    { "type": "DEFAULT", "defaultValue": "'active'" }
                  ]
                },
                {
                  "name": "tags",
                  "dataType": { "name": "SET", "values": ["urgent","internal","external"] },
                  "indexed": false,
                  "constraints": []
                }
              ]
            }
          ]
        }
    """
)
public class SchemaDTO {
    @Schema(description = "Name of the database schema", example = "my_database")
    private String schemaName;

    @ArraySchema(
            arraySchema = @Schema(description = "List of entities (tables) in the schema"),
            schema = @Schema(implementation = EntityDTO.class)
    )
    private List<EntityDTO> entities;
}