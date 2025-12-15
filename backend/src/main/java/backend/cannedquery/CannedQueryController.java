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

import backend.security.AuthUser;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/canned-queries")
@RequiredArgsConstructor
public class CannedQueryController {

    private final CannedQueryService cannedQueryService;

    @GetMapping("/database/{databaseId}")
    public ResponseEntity<?> getAllQueriesByDatabase(
            @PathVariable Integer databaseId,
            @AuthenticationPrincipal AuthUser authUser) {
        try {
            List<CannedQueryDto> queries = cannedQueryService.getAllQueriesByDatabase(databaseId);
            return ResponseEntity.ok(queries);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching queries: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/database/{databaseId}")
    public ResponseEntity<?> getQueryById(
            @PathVariable Integer id,
            @PathVariable Integer databaseId,
            @AuthenticationPrincipal AuthUser authUser) {
        try {
            CannedQueryDto query = cannedQueryService.getQueryById(id, databaseId);
            return ResponseEntity.ok(query);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching query: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createQuery(
            @RequestBody CannedQueryDto dto,
            @AuthenticationPrincipal AuthUser authUser) {
        try {
            CannedQueryDto created = cannedQueryService.createQuery(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating query: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateQuery(
            @PathVariable Integer id,
            @RequestBody CannedQueryDto dto,
            @AuthenticationPrincipal AuthUser authUser) {
        try {
            CannedQueryDto updated = cannedQueryService.updateQuery(id, dto);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating query: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}/database/{databaseId}")
    public ResponseEntity<?> deleteQuery(
            @PathVariable Integer id,
            @PathVariable Integer databaseId,
            @AuthenticationPrincipal AuthUser authUser) {
        try {
            cannedQueryService.deleteQuery(id, databaseId);
            return ResponseEntity.ok("Query deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting query: " + e.getMessage());
        }
    }
}