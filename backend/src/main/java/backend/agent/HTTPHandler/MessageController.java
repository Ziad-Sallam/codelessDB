package backend.agent.HTTPHandler;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import backend.agent.WebSocketHandler.AgentMessageDTO;
import backend.agent.WebSocketHandler.ClientResponseDTO;
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
@RequestMapping("/agent")
@RequiredArgsConstructor
@Tag(name = "AI Agent", description = "Endpoints for interacting with the AI agent for database queries and operations")
public class MessageController {

    private final MessageService messageService;

    @PostMapping("/send")
    @Operation(summary = "Send message to AI agent", description = "Sends a natural language query or command to the AI agent for processing against a database")
    @ApiResponse(responseCode = "200", description = "Returns AI agent response with query results")
    @ApiResponse(responseCode = "401", description = "Unauthorized", 
                 content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                 examples = @ExampleObject(name = "Auth Error", value = "{\"message\": \"Full authentication is required to access this resource\", \"status\": 401}")))
    public ResponseEntity<ClientResponseDTO> sendToUser(
            @Parameter(description = "Message content and database ID") @RequestBody MessageDTO request,
            @AuthenticationPrincipal AuthUser user) {

        if (user == null)
            throw new RuntimeException("Unauthorized");

        AgentMessageDTO message = new AgentMessageDTO("Server", request.getContent());

        ClientResponseDTO res = messageService.runQuery(
                request.getDatabaseId(),
                message,
                user.userId());

        return ResponseEntity.ok(res);
    }

    @GetMapping("/is-database-online")
    @Operation(summary = "Check database status", description = "Verifies if a database container is running and accessible")
    @ApiResponse(responseCode = "200", description = "Returns true if database is online, false otherwise")
    public ResponseEntity<Boolean> isDatabaseOnline(
            @Parameter(description = "Database ID to check") @RequestParam int databaseId,
            @AuthenticationPrincipal AuthUser user) {

        if (user == null)
            throw new RuntimeException("Unauthorized");

        boolean online = messageService.databaseIsOnline(
                user.userId(),
                databaseId);

        return ResponseEntity.ok(online);
    }

    @GetMapping("/create-container")
    @Operation(summary = "Download container creation agent", description = "Downloads the agent executable for creating database containers")
    @ApiResponse(responseCode = "200", description = "Returns executable file")
    public ResponseEntity<Resource> downloadCreateContainer() throws IOException {
        Path path = Paths.get(
                "backend/uploads/agent/dist/create_container.exe")
                .toAbsolutePath();

        Resource resource = new UrlResource(path.toUri());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @GetMapping("/communicate")
    @Operation(summary = "Download communication agent", description = "Downloads the agent executable for database communication")
    @ApiResponse(responseCode = "200", description = "Returns executable file")
    public ResponseEntity<Resource> downloadCommunicate() throws IOException {
        Path path = Paths.get(
                "backend/uploads/agent/dist/communicate.exe")
                .toAbsolutePath();

        Resource resource = new UrlResource(path.toUri());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}
