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
    public ResponseEntity<List<CannedQueryDto>> getAllQueriesByDatabase(
            @PathVariable Integer databaseId,
            @AuthenticationPrincipal AuthUser authUser) {

        List<CannedQueryDto> queries = cannedQueryService.getAllQueriesByDatabase(databaseId);
        return ResponseEntity.ok(queries);
    }

    @GetMapping("/{id}/database/{databaseId}")
    public ResponseEntity<CannedQueryDto> getQueryById(
            @PathVariable Integer id,
            @PathVariable Integer databaseId,
            @AuthenticationPrincipal AuthUser authUser) {

        CannedQueryDto query = cannedQueryService.getQueryById(id, databaseId);
        return ResponseEntity.ok(query);
    }

    @PostMapping
    public ResponseEntity<CannedQueryDto> createQuery(
            @RequestBody CannedQueryDto dto,
            @AuthenticationPrincipal AuthUser authUser) {

        CannedQueryDto created = cannedQueryService.createQuery(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CannedQueryDto> updateQuery(
            @PathVariable Integer id,
            @RequestBody CannedQueryDto dto,
            @AuthenticationPrincipal AuthUser authUser) {

        CannedQueryDto updated = cannedQueryService.updateQuery(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}/database/{databaseId}")
    public ResponseEntity<Void> deleteQuery(
            @PathVariable Integer id,
            @PathVariable Integer databaseId,
            @AuthenticationPrincipal AuthUser authUser) {

        cannedQueryService.deleteQuery(id, databaseId);
        return ResponseEntity.noContent().build();
    }
}