package backend.publicDiagramManagement.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import backend.publicDiagramManagement.dto.PageResponse;
import backend.publicDiagramManagement.dto.PublicDiagramInfoDto;
import backend.publicDiagramManagement.dto.user.PublicUserDto;
import backend.publicDiagramManagement.service.PublicUserService;
import backend.security.AuthUser;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/publicUsers")
@RequiredArgsConstructor
public class PublicUserController {

    private final PublicUserService publicUserService;

    @GetMapping("/designerProfile/{userName}")
    public ResponseEntity<?> getDesignerProfile(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable @NonNull String userName) {

        PublicUserDto publicUserDto = publicUserService.getDesignerProfile(userName);

        return ResponseEntity.ok(publicUserDto);
    }

    @GetMapping("/publicDiagrams/{userName}")
    public ResponseEntity<?> getPublicDiagrams(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            @PathVariable @NonNull String userName) {

        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<PublicDiagramInfoDto> publicUserDto = publicUserService.getPublicDiagrams(userName, pageable);

        return ResponseEntity.ok(new PageResponse<>(publicUserDto));
    }

    @GetMapping("/staredDiagrams/{userName}")
    public ResponseEntity<?> getStaredDiagram(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            @PathVariable @NonNull String userName) {

        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<PublicDiagramInfoDto> staredDiagrams = publicUserService.getStaredPublicDiagrams(userName, pageable);
        return ResponseEntity.ok(new PageResponse<>(staredDiagrams));
    }
}
