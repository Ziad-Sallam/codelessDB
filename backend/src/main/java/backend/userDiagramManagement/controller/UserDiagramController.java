package backend.userDiagramManagement.controller;

import backend.security.AuthUser;
import backend.userDiagramManagement.dto.*;
import backend.userDiagramManagement.dto.create.DiagramCreateRequestDto;
import backend.userDiagramManagement.dto.search.DiagramSearchRequestDto;
import backend.userDiagramManagement.dto.share.DiagramShareRequestDto;
import backend.userDiagramManagement.dto.share.DiagramShareResponseDto;
import backend.userDiagramManagement.dto.update.DiagramUpdateRequestDto;
import backend.userDiagramManagement.service.IUserDiagramService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@RestController
@RequestMapping("/diagram")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class UserDiagramController {

    private final IUserDiagramService userDiagramService;

    @PostMapping("/create")
    public ResponseEntity<?> createDiagram(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody DiagramCreateRequestDto request) {

        int userId = authUser.userId();
        UUID createdDiagramID = userDiagramService.createDiagram(userId, request);

        return ResponseEntity.ok(createdDiagramID);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateDiagram(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable UUID id,
            @RequestBody DiagramUpdateRequestDto request) {

        int userId = authUser.userId();
        request.setId(id);

        Date updateDate = userDiagramService.updateDiagram(userId, request);

        return ResponseEntity.ok(updateDate);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteDiagram(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable UUID id) {

        int userId = authUser.userId();
        userDiagramService.deleteDiagram(userId, id);

        return ResponseEntity.ok("Diagram with id " + id + " has been deleted successfully");
    }

    @GetMapping("/search/{id}")
    public ResponseEntity<?> searchDiagramById(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable UUID id) {

        int userId = authUser.userId();
        DiagramDto result = userDiagramService.searchDiagramById(userId, id);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchDiagrams(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody DiagramSearchRequestDto request,
            Pageable pageable) {

        int userId = authUser.userId();

        Page<DiagramDto> result = userDiagramService.searchDiagrams(userId, request, pageable);

        return ResponseEntity.ok(result);
    }

    @PutMapping("/share")
    public ResponseEntity<?> shareDiagram(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody DiagramShareRequestDto request) {

        int userId = authUser.userId();
        DiagramShareResponseDto responseDto = userDiagramService.shareDiagram(userId, request);

        return ResponseEntity.ok(responseDto);
    }
}
