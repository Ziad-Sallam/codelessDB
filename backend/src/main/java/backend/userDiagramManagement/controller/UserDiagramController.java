package backend.userDiagramManagement.controller;

import backend.security.AuthUser;
import backend.userDiagramManagement.dto.*;
import backend.userDiagramManagement.dto.create.DiagramCreateRequestDto;
import backend.userDiagramManagement.dto.create.DiagramCreateResponseDto;
import backend.userDiagramManagement.dto.search.DiagramSearchRequestDto;
import backend.userDiagramManagement.dto.share.DiagramShareRequestDto;
import backend.userDiagramManagement.dto.share.DiagramShareResponseDto;
import backend.userDiagramManagement.dto.update.DiagramUpdateRequestDto;
import backend.userDiagramManagement.dto.update.DiagramUpdateResponseDto;
import backend.userDiagramManagement.dto.delete.DiagramDeleteResponseDto;
import backend.userDiagramManagement.service.IUserDiagramService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.UUID;

@RestController
@RequestMapping("/diagrams")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class UserDiagramController {

    private final IUserDiagramService userDiagramService;

    private int id(AuthUser authUser) {
        return authUser.userId();
    }

    @PostMapping("/get")
    public ResponseEntity<Page<DiagramInfoDto>> getDiagramsByUserId(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(defaultValue = "0") int pageNumber, @RequestParam(defaultValue = "10") int pageSize) {

        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<DiagramInfoDto> result = userDiagramService.getDiagramsByUserId(id(authUser), pageable);

        return ResponseEntity.ok(result);
    }

    @PostMapping("/create")
    public ResponseEntity<DiagramCreateResponseDto> createDiagram(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody DiagramCreateRequestDto request) {

        UUID diagramId = userDiagramService.createDiagram(id(authUser), request);

        return ResponseEntity.ok(new DiagramCreateResponseDto(
                "Diagram created successfully",
                diagramId
        ));
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
                updateDate
        ));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<DiagramDeleteResponseDto> deleteDiagram(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable UUID id) {

        userDiagramService.deleteDiagram(id(authUser), id);

        return ResponseEntity.ok(new DiagramDeleteResponseDto(
                "Diagram deleted successfully",
                id
        ));
    }

    @GetMapping("/search/{id}")
    public ResponseEntity<DiagramDto> searchDiagramById(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable UUID id) {

        DiagramDto result = userDiagramService.searchDiagramById(id(authUser), id);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/search")
    public ResponseEntity<Page<DiagramDto>> searchDiagrams(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam int pageNumber,
            @RequestParam int pageSize,
            @RequestBody DiagramSearchRequestDto request) {

        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<DiagramDto> result = userDiagramService.searchDiagrams(id(authUser), request, pageable);

        return ResponseEntity.ok(result);
    }

    @PutMapping("/share")
    public ResponseEntity<DiagramShareResponseDto> shareDiagram(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody DiagramShareRequestDto request) {

        DiagramShareResponseDto response = userDiagramService.shareDiagram(id(authUser), request);
        return ResponseEntity.ok(response);
    }
}
