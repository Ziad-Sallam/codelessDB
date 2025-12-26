package backend.collab.snapshot;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import backend.config.ErrorResponse;
import backend.security.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/snapshot")
@RequiredArgsConstructor
@Tag(name = "Collaboration", description = "Endpoints for real-time collaborative editing and snapshot management")
public class SnapshotController {

	private final SnapshotService snapshotService;

	@GetMapping("/{id}/meta")
	@Operation(summary = "Get diagram metadata", description = "Retrieves metadata for a diagram snapshot including version info and timestamps")
	@ApiResponse(responseCode = "200", description = "Returns snapshot metadata")
	@ApiResponse(responseCode = "403", description = "User does not have access to this diagram", 
	             content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	@ApiResponse(responseCode = "404", description = "Diagram not found", 
	             content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	public ResponseEntity<SnapshotDto> getDiagramMetadata(
			@AuthenticationPrincipal AuthUser authUser,
			@Parameter(description = "UUID of the diagram") @PathVariable UUID id) {

		SnapshotDto result = snapshotService.getDiagramMetadata(authUser.userId(), id);
		return ResponseEntity.ok(result);
	}

	@GetMapping("/{id}/binary")
	@Operation(summary = "Get diagram snapshot binary", description = "Retrieves the binary snapshot data for collaborative editing synchronization")
	@ApiResponse(responseCode = "200", description = "Returns binary snapshot data")
	@ApiResponse(responseCode = "403", description = "User does not have access to this diagram", 
	             content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	@ApiResponse(responseCode = "404", description = "Diagram not found", 
	             content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	public ResponseEntity<byte[]> getDiagramSnapshot(
			@AuthenticationPrincipal AuthUser authUser,
			@Parameter(description = "UUID of the diagram") @PathVariable UUID id) {

		byte[] result = snapshotService.getDiagramSnapshot(authUser.userId(), id);
		return ResponseEntity.ok()
				.contentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM)
				.body(result);
	}
}
