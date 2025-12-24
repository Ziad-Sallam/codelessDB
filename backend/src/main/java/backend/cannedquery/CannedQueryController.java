package backend.cannedquery;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import backend.config.ErrorResponse;
import backend.security.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/canned-queries")
@RequiredArgsConstructor
@Tag(name = "Canned Queries", description = "Endpoints for managing saved SQL query templates and snippets")
public class CannedQueryController {

    private final CannedQueryService cannedQueryService;

    @GetMapping("/database/{databaseId}")
    @Operation(summary = "Get all queries for a database", description = "Retrieve all saved SQL query templates for a specific database")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List of canned queries",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CannedQueryDto.class),
                            examples = @ExampleObject(value = "[{\"id\":1,\"name\":\"Get All Users\",\"description\":\"Fetches all users\",\"query\":\"SELECT * FROM users\",\"createdAt\":\"2025-12-24T17:00:00\",\"updatedAt\":\"2025-12-24T17:10:00\",\"databaseId\":1,\"databaseName\":\"UserDB\"}]")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Database not found",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"message\":\"Database with ID 1 not found\",\"status\":404}")
                    )
            )
    })
    public ResponseEntity<List<CannedQueryDto>> getAllQueriesByDatabase(
            @Parameter(description = "ID of the database", required = true) @PathVariable Integer databaseId,
            @AuthenticationPrincipal AuthUser authUser) {

        List<CannedQueryDto> queries = cannedQueryService.getAllQueriesByDatabase(databaseId);
        return ResponseEntity.ok(queries);
    }

    @GetMapping("/{id}/database/{databaseId}")
    @Operation(summary = "Get query by ID", description = "Retrieve a specific saved query template by its ID")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Query details",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CannedQueryDto.class),
                            examples = @ExampleObject(value = "{\"id\":1,\"name\":\"Get All Users\",\"description\":\"Fetches all users\",\"query\":\"SELECT * FROM users\",\"createdAt\":\"2025-12-24T17:00:00\",\"updatedAt\":\"2025-12-24T17:10:00\",\"databaseId\":1,\"databaseName\":\"UserDB\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Query not found",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"message\":\"The requested resource was not found\",\"status\":404}")
                    )
            )
    })
    public ResponseEntity<CannedQueryDto> getQueryById(
            @Parameter(description = "Query ID", required = true) @PathVariable Integer id,
            @Parameter(description = "Database ID", required = true) @PathVariable Integer databaseId,
            @AuthenticationPrincipal AuthUser authUser) {

        CannedQueryDto query = cannedQueryService.getQueryById(id, databaseId);
        return ResponseEntity.ok(query);
    }

    @PostMapping
    @Operation(summary = "Create a canned query", description = "Save a new SQL query template for reuse")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Query created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CannedQueryDto.class),
                            examples = @ExampleObject(value = "{\"id\":1,\"name\":\"Get All Users\",\"description\":\"Fetches all users\",\"query\":\"SELECT * FROM users\",\"createdAt\":\"2025-12-24T17:00:00\",\"updatedAt\":\"2025-12-24T17:00:00\",\"databaseId\":1,\"databaseName\":\"UserDB\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"message\":\"Query name is required\",\"status\":400}")
                    )
            )
    })
    public ResponseEntity<CannedQueryDto> createQuery(
            @Parameter(description = "Query details including SQL and metadata", required = true,
                    example = "{\"name\":\"Get All Users\",\"description\":\"Fetches all users\",\"query\":\"SELECT * FROM users\",\"databaseId\":1}")
            @RequestBody CannedQueryDto dto,
            @AuthenticationPrincipal AuthUser authUser) {

        CannedQueryDto created = cannedQueryService.createQuery(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a canned query", description = "Update an existing saved query template")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Query updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CannedQueryDto.class),
                            examples = @ExampleObject(value = "{\"id\":1,\"name\":\"Get Users Updated\",\"description\":\"Fetches all users\",\"query\":\"SELECT * FROM users\",\"createdAt\":\"2025-12-24T17:00:00\",\"updatedAt\":\"2025-12-24T17:20:00\",\"databaseId\":1,\"databaseName\":\"UserDB\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Query not found",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"message\":\"The requested resource was not found\",\"status\":404}")
                    )
            )
    })
    public ResponseEntity<CannedQueryDto> updateQuery(
            @Parameter(description = "Query ID", required = true) @PathVariable Integer id,
            @Parameter(description = "Updated query details", required = true,
                    example = "{\"name\":\"Get Users Updated\",\"description\":\"Fetches all users\",\"query\":\"SELECT * FROM users\",\"databaseId\":1}")
            @RequestBody CannedQueryDto dto,
            @AuthenticationPrincipal AuthUser authUser) {

        CannedQueryDto updated = cannedQueryService.updateQuery(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}/database/{databaseId}")
    @Operation(summary = "Delete a canned query", description = "Permanently delete a saved query template")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Query deleted successfully"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Query not found",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"message\":\"The requested resource was not found\",\"status\":404}")
                    )
            )
    })
    public ResponseEntity<Void> deleteQuery(
            @Parameter(description = "Query ID", required = true) @PathVariable Integer id,
            @Parameter(description = "Database ID", required = true) @PathVariable Integer databaseId,
            @AuthenticationPrincipal AuthUser authUser) {

        cannedQueryService.deleteQuery(id, databaseId);
        return ResponseEntity.noContent().build();
    }
}
