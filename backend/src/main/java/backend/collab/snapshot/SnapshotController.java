package backend.collab.snapshot;

import java.util.Base64;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import backend.security.AuthUser;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/snapshot")
@RequiredArgsConstructor
public class SnapshotController {

	private final SnapshotService snapshotService;

	@GetMapping("/{id}")
	public ResponseEntity<?> getLatestDiagram(
			@AuthenticationPrincipal AuthUser authUser,
			@PathVariable UUID id) {

		SnapshotDto result = snapshotService.getLatestDiagram(authUser.userId(), id);
		return ResponseEntity.ok(result);
	}
}
