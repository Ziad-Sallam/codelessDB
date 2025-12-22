package backend.collab.snapshot;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import backend.security.AuthUser;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/snapshot")
@RequiredArgsConstructor
public class SnapshotController {

	private final SnapshotService snapshotService;

	@GetMapping("/{id}/meta")
	public ResponseEntity<SnapshotDto> getDiagramMetadata(
			@AuthenticationPrincipal AuthUser authUser,
			@PathVariable UUID id) {

		SnapshotDto result = snapshotService.getDiagramMetadata(authUser.userId(), id);
		return ResponseEntity.ok(result);
	}

	@GetMapping("/{id}/binary")
	public ResponseEntity<byte[]> getDiagramSnapshot(
			@AuthenticationPrincipal AuthUser authUser,
			@PathVariable UUID id) {

		byte[] result = snapshotService.getDiagramSnapshot(authUser.userId(), id);
		return ResponseEntity.ok()
				.contentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM)
				.body(result);
	}
}
