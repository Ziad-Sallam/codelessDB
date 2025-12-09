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
import backend.collab.update.DiagramUpdateRequestDto;
import backend.collab.update.DiagramUpdateResponseDto;
import backend.security.AuthUser;
import backend.userDiagramManagement.dto.DiagramDto;
import backend.userDiagramManagement.service.IUserDiagramService;
import lombok.RequiredArgsConstructor;
import reactor.core.scheduler.Schedulers.Snapshot;

@RestController
@RequestMapping("/room")
@RequiredArgsConstructor
public class RoomController {

	private final SnapshotService userDiagramService;

	private int id(AuthUser authUser) {
		return authUser.userId();
	}

	// @GetMapping("/search/{id}")
	@GetMapping("/{id}")
	public ResponseEntity<DiagramDto> searchDiagramById(
			@AuthenticationPrincipal AuthUser authUser,
			@PathVariable UUID id) {

		DiagramDto result = userDiagramService.searchDiagramById(id(authUser), id);
		return ResponseEntity.ok(result);
	}

	@PutMapping("/update/{id}")
	public ResponseEntity<DiagramUpdateResponseDto> updateDiagram(
			@AuthenticationPrincipal AuthUser authUser,
			@PathVariable UUID id,
			@RequestBody DiagramUpdateRequestDto request) {

		Date updateDate = userDiagramService.updateDiagram(id(authUser), request, id);

		return ResponseEntity.ok(new DiagramUpdateResponseDto(
				"Diagram updated successfully",
				id,
				updateDate));
	}

}
