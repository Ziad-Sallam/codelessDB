package backend.SQLGeneration.service;

import backend.SQLGeneration.dto.SchemaDTO;

public interface SchemaService {
    /**
     * Generates SQL DDL statements for the given schema.
     * @param schemaDTO the schema definition
     * @return the generated SQL DDL
     */
    String generateDDL(SchemaDTO schemaDTO);
}