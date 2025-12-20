package backend.userDiagramManagement.controller;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import backend.security.AuthUser;
import backend.userDiagramManagement.dto.DiagramDto;
import backend.userDiagramManagement.dto.DiagramInfoDto;
import backend.userDiagramManagement.dto.create.DiagramCreateRequestDto;
import backend.userDiagramManagement.dto.delete.DiagramDeleteResponseDto;
import backend.publicDiagramManagement.dto.PageResponse;
import backend.userDiagramManagement.dto.search.DiagramSearchRequestDto;
import backend.userDiagramManagement.dto.share.DiagramShareRequestDto;
import backend.userDiagramManagement.dto.share.DiagramShareResponseDto;
import backend.userDiagramManagement.dto.update.DiagramUpdateRequestDto;
import backend.userDiagramManagement.dto.update.DiagramUpdateResponseDto;
import backend.userDiagramManagement.service.IUserDiagramService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/diagrams")
@RequiredArgsConstructor
public class UserDiagramController {

    private final IUserDiagramService userDiagramService;

    private int id(AuthUser authUser) {
        return authUser.userId();
    }

    @GetMapping("/get")
    public ResponseEntity<?> getDiagramsByUserId(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize) {

        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<DiagramInfoDto> result = userDiagramService.getDiagramsByUserId(id(authUser), pageable);

        return ResponseEntity.ok(new PageResponse<>(result));
    }

    @PostMapping("/create")
    public ResponseEntity<DiagramInfoDto> createDiagram(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody DiagramCreateRequestDto request) {

        DiagramInfoDto diagram = userDiagramService.createDiagram(id(authUser), request);

        return ResponseEntity.ok(diagram);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<DiagramUpdateResponseDto> updateDiagram(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable UUID id,
            @RequestBody DiagramUpdateRequestDto request) {

        LocalDateTime updateDate = userDiagramService.updateDiagram(id(authUser), request, id);

        return ResponseEntity.ok(new DiagramUpdateResponseDto(
                "Diagram updated successfully",
                id,
                updateDate));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<DiagramDeleteResponseDto> deleteDiagram(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable UUID id) {

        userDiagramService.deleteDiagram(id(authUser), id);

        return ResponseEntity.ok(new DiagramDeleteResponseDto(
                "Diagram deleted successfully",
                id));
    }

    @GetMapping("/search/{id}")
    public ResponseEntity<DiagramDto> searchDiagramById(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable UUID id) {

        DiagramDto result = userDiagramService.searchDiagramById(id(authUser), id);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/search")
    public ResponseEntity<?> searchDiagrams(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam int pageNumber,
            @RequestParam int pageSize,
            @RequestBody DiagramSearchRequestDto request) {

        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<DiagramInfoDto> result = userDiagramService.searchDiagrams(id(authUser), request, pageable);

        return ResponseEntity.ok(new PageResponse<>(result));
    }

    @PutMapping("/share/{id}")
    public ResponseEntity<DiagramShareResponseDto> shareDiagram(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable UUID id,
            @RequestBody DiagramShareRequestDto request) {

        DiagramShareResponseDto response = userDiagramService.shareDiagram(id(authUser), id, request);
        return ResponseEntity.ok(response);
    }
}
