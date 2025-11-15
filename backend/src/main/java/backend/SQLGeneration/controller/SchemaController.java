package backend.SQLGeneration.controller;

import backend.SQLGeneration.dto.SchemaDTO;
import backend.SQLGeneration.service.SchemaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/schema")
@RequiredArgsConstructor
public class SchemaController {

    private final SchemaService schemaService;

    @PostMapping("/generate")
    public ResponseEntity<String> generateDDL(@RequestBody SchemaDTO schemaDTO) {
        System.out.println("==== MAPPED SCHEMADTO ====");
        System.out.println(schemaDTO);
        System.out.println("==========================");
        String ddl = schemaService.generateDDL(schemaDTO);
        return ResponseEntity.ok(ddl);
    }
}
