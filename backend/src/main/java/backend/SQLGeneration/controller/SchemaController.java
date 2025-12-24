package backend.SQLGeneration.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import backend.SQLGeneration.dto.SchemaDTO;
import backend.SQLGeneration.service.SchemaService;
import backend.SQLGeneration.service.util.SchemaValidationException;
import backend.security.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
@Tag(name = "SQL Generation", description = "Endpoints for generating SQL DDL from diagram data")
public class SchemaController {

    private final SchemaService schemaService;

    @PostMapping("/generate")
    @Operation(summary = "Generate DDL from schema", description = "Converts the visual diagram schema into a SQL DDL (Data Definition Language) script")
    @ApiResponse(responseCode = "200", description = "Returns the generated SQL DDL script", 
                 content = @Content(schema = @Schema(implementation = String.class),
                 examples = @ExampleObject(value = "CREATE TABLE users (\n  id INT PRIMARY KEY AUTO_INCREMENT,\n  username VARCHAR(255) NOT NULL UNIQUE,\n  email VARCHAR(255) NOT NULL\n);")))
    @ApiResponse(responseCode = "400", description = "Invalid schema data",
                 content = @Content(schema = @Schema(implementation = String.class),
                 examples = @ExampleObject(name = "Validation Error", value = "Schema validation error: {error message}")))
    @ApiResponse(responseCode = "500", description = "Failed to generate DDL",
                 content = @Content(schema = @Schema(implementation = String.class),
                 examples = @ExampleObject(name = "Internal Server Error", value = "Unexpected error: {error message}")))
    public ResponseEntity<String> generateDDL(@AuthenticationPrincipal AuthUser authUser,
            @RequestBody SchemaDTO schemaDTO) {
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
