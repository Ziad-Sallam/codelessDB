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
import backend.security.AuthUser;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/agent")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping("/send")
    public ResponseEntity<ClientResponseDTO> sendToUser(
            @RequestBody MessageDTO request,
            @AuthenticationPrincipal AuthUser user) {

        AgentMessageDTO message = new AgentMessageDTO("Server", request.getContent());

        ClientResponseDTO res = messageService.runQuery(
                request.getDatabaseId(),
                message,
                user.userId());

        return ResponseEntity.ok(res);
    }

    @GetMapping("/is-database-online")
    public ResponseEntity<Boolean> isDatabaseOnline(
            @RequestParam int databaseId,
            @AuthenticationPrincipal AuthUser user) {

        boolean online = messageService.databaseIsOnline(
                user.userId(),
                databaseId);

        return ResponseEntity.ok(online);
    }

    @GetMapping("/create-container")
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
