package backend.cannedquery;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import backend.config.ErrorResponse;
import backend.security.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/canned-queries")
@RequiredArgsConstructor
@Tag(name = "Canned Queries", description = "Endpoints for managing saved SQL query templates and snippets")
public class CannedQueryController {

    private final CannedQueryService cannedQueryService;

    @GetMapping("/database/{databaseId}")
    @Operation(summary = "Get queries for database", description = "Retrieves all saved query templates for a specific database")
    @ApiResponse(responseCode = "200", description = "Returns list of canned queries", 
                 content = @Content(schema = @Schema(implementation = CannedQueryDto.class)))
    public ResponseEntity<List<CannedQueryDto>> getAllQueriesByDatabase(
            @Parameter(description = "ID of the database") @PathVariable Integer databaseId,
            @AuthenticationPrincipal AuthUser authUser) {

        List<CannedQueryDto> queries = cannedQueryService.getAllQueriesByDatabase(databaseId);
        return ResponseEntity.ok(queries);
    }

    @GetMapping("/{id}/database/{databaseId}")
    @Operation(summary = "Get query by ID", description = "Retrieves a specific canned query template by its ID")
    @ApiResponse(responseCode = "200", description = "Returns the query details")
    @ApiResponse(responseCode = "404", description = "Query not found", 
                 content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                 examples = @ExampleObject(value = "{\"message\": \"The requested resource was not found\", \"status\": 404}")))
    public ResponseEntity<CannedQueryDto> getQueryById(
            @Parameter(description = "Query ID") @PathVariable Integer id,
            @Parameter(description = "Database ID") @PathVariable Integer databaseId,
            @AuthenticationPrincipal AuthUser authUser) {

        CannedQueryDto query = cannedQueryService.getQueryById(id, databaseId);
        return ResponseEntity.ok(query);
    }

    @PostMapping
    @Operation(summary = "Create a canned query", description = "Saves a new SQL query template for reuse")
    @ApiResponse(responseCode = "201", description = "Query created successfully")
    public ResponseEntity<CannedQueryDto> createQuery(
            @Parameter(description = "Query details including SQL and metadata") @RequestBody CannedQueryDto dto,
            @AuthenticationPrincipal AuthUser authUser) {

        CannedQueryDto created = cannedQueryService.createQuery(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a canned query", description = "Updates an existing saved query template")
    @ApiResponse(responseCode = "200", description = "Query updated successfully")
    @ApiResponse(responseCode = "404", description = "Query not found", 
                 content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                 examples = @ExampleObject(value = "{\"message\": \"The requested resource was not found\", \"status\": 404}")))
    public ResponseEntity<CannedQueryDto> updateQuery(
            @Parameter(description = "Query ID") @PathVariable Integer id,
            @Parameter(description = "Updated query details") @RequestBody CannedQueryDto dto,
            @AuthenticationPrincipal AuthUser authUser) {

        CannedQueryDto updated = cannedQueryService.updateQuery(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}/database/{databaseId}")
    @Operation(summary = "Delete a canned query", description = "Permanently deletes a saved query template")
    @ApiResponse(responseCode = "204", description = "Query deleted successfully")
    @ApiResponse(responseCode = "404", description = "Query not found", 
                 content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                 examples = @ExampleObject(value = "{\"message\": \"The requested resource was not found\", \"status\": 404}")))
    public ResponseEntity<Void> deleteQuery(
            @Parameter(description = "Query ID") @PathVariable Integer id,
            @Parameter(description = "Database ID") @PathVariable Integer databaseId,
            @AuthenticationPrincipal AuthUser authUser) {

        cannedQueryService.deleteQuery(id, databaseId);
        return ResponseEntity.noContent().build();
    }
}