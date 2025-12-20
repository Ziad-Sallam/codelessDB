package backend.collaboration;

import java.util.UUID;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import backend.security.AuthUser;
import backend.security.JwtExtractor;
import backend.entities.joins.UserDiagram;
import backend.userDiagramManagement.repository.UserDiagramRepository;

@RestController
@RequestMapping("/api/diagrams")
@CrossOrigin(origins = "*") 
public class DiagramController {

    @Autowired
    private DiagramService diagramService;

    @Autowired
    private UserDiagramRepository userDiagramRepository;

    @Autowired
    private JwtExtractor jwtExtractor;

    // 1. Save Snapshot (Node.js -> Spring)
    @PutMapping("/{id}/snapshot")
    public ResponseEntity<String> updateSnapshot(
            @PathVariable UUID id, 
            @RequestBody byte[] snapshotData 
    ) {
        try {
            diagramService.saveSnapshot(id, snapshotData);
            return ResponseEntity.ok("Snapshot saved successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error saving snapshot: " + e.getMessage());
        }
    }

    // 2. Load Snapshot
    @GetMapping("/{id}/snapshot")
    public ResponseEntity<byte[]> getSnapshot(@PathVariable UUID id) {
        try {
            byte[] data = diagramService.getSnapshot(id);
            if (data == null || data.length == 0) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(data);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // --- 3. NEW: Permission Check (Node.js -> Spring) ---
    @GetMapping("/{id}/permission")
    public ResponseEntity<String> checkPermission(
            @PathVariable UUID id, 
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader
    ) {
        try {
            // 1. Validate Token
            AuthUser authUser = jwtExtractor.authenticate(authHeader, true);
            if (authUser == null) return ResponseEntity.status(401).body("INVALID_TOKEN");

            // 2. Check DB for access
            Optional<UserDiagram> access = userDiagramRepository.findByUser_IdAndDiagram_Id(authUser.userId(), id);

            if (access.isEmpty()) {
                return ResponseEntity.status(403).body("NONE");
            }

            // 3. Return Role (WRITER, READER, OWNER)
            return ResponseEntity.ok(access.get().getRole().toString());

        } catch (Exception e) {
            return ResponseEntity.status(401).body("UNAUTHORIZED");
        }
    }
}