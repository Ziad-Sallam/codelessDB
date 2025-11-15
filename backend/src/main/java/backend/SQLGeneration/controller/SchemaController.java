package backend.SQLGeneration.controller;

import backend.SQLGeneration.dto.SchemaDTO;
import backend.SQLGeneration.service.SchemaService;
import backend.SQLGeneration.service.util.SchemaValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class SchemaController {

    private final SchemaService schemaService;

    @PostMapping("/generate")
    public ResponseEntity<String> generateDDL(@RequestBody SchemaDTO schemaDTO) {
        try {
            String ddl = schemaService.generateDDL(schemaDTO);
            return ResponseEntity.ok(ddl);
        } catch (SchemaValidationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Schema validation error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error: " + e.getMessage());
        }
    }
}
