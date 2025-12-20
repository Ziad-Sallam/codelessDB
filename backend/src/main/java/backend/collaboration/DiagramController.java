package backend.collaboration;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/diagrams")
@CrossOrigin(origins = "*") // Allows Node.js (running on different port) to call this
public class DiagramController {

    @Autowired
    private DiagramService diagramService;

    // 1. Endpoint for Node.js to SAVE the snapshot
    // PUT /api/diagrams/{id}/snapshot
    @PutMapping("/{id}/snapshot")
    public ResponseEntity<String> updateSnapshot(
            @PathVariable UUID id, 
            @RequestBody byte[] snapshotData // Spring maps the raw body bytes here
    ) {
        try {
            diagramService.saveSnapshot(id, snapshotData);
            return ResponseEntity.ok("Snapshot saved successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error saving snapshot: " + e.getMessage());
        }
    }

    // 2. Endpoint for Node.js to LOAD the snapshot
    // GET /api/diagrams/{id}/snapshot
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
}