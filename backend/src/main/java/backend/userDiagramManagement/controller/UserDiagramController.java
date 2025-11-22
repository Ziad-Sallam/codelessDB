package backend.userDiagramManagement.controller;

import backend.security.AuthUser;
import backend.userDiagramManagement.dto.*;
import backend.userDiagramManagement.dto.create.DiagramCreateRequestDto;
import backend.userDiagramManagement.dto.create.DiagramCreateResponseDto;
import backend.userDiagramManagement.dto.get.DiagramGetInfoRequestDto;
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
@RequestMapping("/diagram")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class UserDiagramController {

    private final IUserDiagramService userDiagramService;

    private int userId(AuthUser authUser) {
        return authUser.userId();
    }

    @PostMapping("/get")
    public ResponseEntity<Page<DiagramInfoDto>> getDiagramsByUserId(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody DiagramGetInfoRequestDto request) {

        Pageable pageable = PageRequest.of(request.getPageNumber(), request.getPageSize());
        Page<DiagramInfoDto> result = userDiagramService.getDiagramsByUserId(userId(authUser), request, pageable);

        return ResponseEntity.ok(result);
    }

    @PostMapping("/create")
    public ResponseEntity<DiagramCreateResponseDto> createDiagram(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody DiagramCreateRequestDto request) {

        UUID diagramId = userDiagramService.createDiagram(userId(authUser), request);

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

        Date updateDate = userDiagramService.updateDiagram(userId(authUser), request, id);

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

        userDiagramService.deleteDiagram(userId(authUser), id);

        return ResponseEntity.ok(new DiagramDeleteResponseDto(
                "Diagram deleted successfully",
                id
        ));
    }

    @GetMapping("/search/{id}")
    public ResponseEntity<DiagramDto> searchDiagramById(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable UUID id) {

        DiagramDto result = userDiagramService.searchDiagramById(userId(authUser), id);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/search")
    public ResponseEntity<Page<DiagramDto>> searchDiagrams(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody DiagramSearchRequestDto request) {

        Pageable pageable = PageRequest.of(request.getPageNumber(), request.getPageSize());
        Page<DiagramDto> result = userDiagramService.searchDiagrams(userId(authUser), request, pageable);

        return ResponseEntity.ok(result);
    }

    @PutMapping("/share")
    public ResponseEntity<DiagramShareResponseDto> shareDiagram(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody DiagramShareRequestDto request) {

        DiagramShareResponseDto response = userDiagramService.shareDiagram(userId(authUser), request);
        return ResponseEntity.ok(response);
    }
}
