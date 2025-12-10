package backend.collab;

import java.util.Date;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import backend.collab.services.SnapshotService;
import backend.collab.updateDto.DiagramUpdateRequestDto;
import backend.collab.updateDto.DiagramUpdateResponseDto;
import backend.security.AuthUser;
import backend.userDiagramManagement.dto.DiagramDto;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/room")
@RequiredArgsConstructor
public class RoomController {

	private final SnapshotService snapshotService;

	// @GetMapping("/search/{id}")
	@GetMapping("/{id}")
	public ResponseEntity<DiagramDto> searchDiagramById(
			@AuthenticationPrincipal AuthUser authUser,
			@PathVariable UUID id) {

		DiagramDto result = snapshotService.searchDiagramById(authUser.userId(), id);
		return ResponseEntity.ok(result);
	}

	@PutMapping("/update/{id}")
	public ResponseEntity<DiagramUpdateResponseDto> saveState(
			@AuthenticationPrincipal AuthUser authUser,
			@PathVariable UUID id,
			@RequestBody DiagramUpdateRequestDto request) {

		Date updateDate = snapshotService.updateDiagram(authUser.userId(), request, id);

		return ResponseEntity.ok(new DiagramUpdateResponseDto(
				"Diagram updated successfully",
				id,
				updateDate));
	}

}
