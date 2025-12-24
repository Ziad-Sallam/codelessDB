package backend.databaseManagement;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import backend.config.ErrorResponse;
import backend.security.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/database")
@RequiredArgsConstructor
@Tag(name = "Database Management", description = "Endpoints for managing user databases and server configurations")
public class DatabaseManagementController {

    private final DatabaseManagementService databaseManagementService;

    @PostMapping("/create-database")
    @Operation(summary = "Create a database", description = "Registers a new database configuration and initiates the database container")
    @ApiResponse(responseCode = "201", description = "Database created successfully")
    @ApiResponse(responseCode = "404", description = "Server configuration not found", 
                 content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                 examples = @ExampleObject(name = "Not Found", value = "{\"message\": \"Server configuration with ID 1 could not be found\", \"status\": 404}")))
    public ResponseEntity<CreateDatabaseDTO> createDatabase(
            @RequestBody CreateDatabaseDTO createDatabaseDTO,
            @AuthenticationPrincipal AuthUser authUser) {
        CreateDatabaseDTO createdDatabase = databaseManagementService.createDatabase(
                createDatabaseDTO,
                authUser.userId());

        return ResponseEntity.status(HttpStatus.CREATED).body(createdDatabase);
    }

    @GetMapping("/get-user-databases")
    @Operation(summary = "Get user databases", description = "Retrieves all databases associated with the authenticated user")
    @ApiResponse(responseCode = "200", description = "Returns list of user databases",
                 content = @Content(schema = @Schema(implementation = SendDatabasesDTO.class)))
    public ResponseEntity<SendDatabasesDTO> getUserDatabases(
            @AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(
                databaseManagementService.getUserDatabases(authUser.userId()));
    }

    @PostMapping("/create-mysql-container")
    @Operation(summary = "Initiate MySQL container", description = "Starts a dedicated MySQL container for the given database ID")
    @ApiResponse(responseCode = "200", description = "Container initiated successfully")
    @ApiResponse(responseCode = "404", description = "Database configuration not found",
                 content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                 examples = @ExampleObject(name = "Initiation Error", value = "{\"message\": \"Cannot initiate: Database ID 55 not found\", \"status\": 404}")))
    public ResponseEntity<InitiateDatabaseDTO> createMysqlContainer(
            @RequestBody int id) {
        return ResponseEntity.ok(
                databaseManagementService.initiateDatabase(id));
    }

    @PostMapping("/create-server")
    @Operation(summary = "Create a server", description = "Registers a new database server for external connections")
    @ApiResponse(responseCode = "201", description = "Server created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid server data",
                 content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                 examples = @ExampleObject(name = "Validation Error", value = "{\"message\": \"Server name cannot be empty\", \"status\": 400}")))
    public ResponseEntity<CreateServerDTO> createServer(
            @RequestBody CreateServerDTO createServerDTO,
            @AuthenticationPrincipal AuthUser authUser) {
        CreateServerDTO createdServer = databaseManagementService.createServer(
                createServerDTO,
                authUser.userId());

        return ResponseEntity.status(HttpStatus.CREATED).body(createdServer);
    }

    @GetMapping("/get-user-servers")
    @Operation(summary = "Get user servers", description = "Retrieves all server configurations owned by the user")
    @ApiResponse(responseCode = "200", description = "Returns list of user servers")
    public ResponseEntity<List<CreateServerDTO>> getUserServers(
            @AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(
                databaseManagementService.getUserServers(authUser.userId()));
    }
}
