package backend.SQLGeneration.service;

import backend.SQLGeneration.dto.SchemaDTO;
import org.springframework.stereotype.Service;

@Service
public class MySQLSchemaService implements SchemaService {
    @Override
    public String generateDDL(SchemaDTO schemaDTO) {
        return "CREATE TABLE helloWorld;";
    }
}
