package backend.publicDiagramManagement.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import backend.publicDiagramManagement.dto.PageResponse;
import backend.publicDiagramManagement.dto.PublicDiagramDto;
import backend.publicDiagramManagement.dto.PublicDiagramInfoDto;
import backend.publicDiagramManagement.dto.get.ToBePublishedDiagramDto;
import backend.publicDiagramManagement.dto.publish.PublishDiagramRequestDto;
import backend.publicDiagramManagement.dto.search.SearchRequestDto;
import backend.publicDiagramManagement.dto.user.PublicUserInfoDto;
import backend.publicDiagramManagement.service.HashtagService;
import backend.publicDiagramManagement.service.PublicDiagramService;
import backend.security.AuthUser;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/publicDiagrams")
@RequiredArgsConstructor
public class PublicDiagramController {

    private final PublicDiagramService publicDiagramService;
    private final HashtagService hashtagService;

    @PostMapping("/publish")
    public ResponseEntity<?> publishDiagram(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody PublishDiagramRequestDto publicDiagramDto) {

        publicDiagramService.publishDiagram(authUser.userId(), publicDiagramDto);
        return ResponseEntity.ok("Diagram published successfully :)");
    }

    @GetMapping("/getToBePublished")
    public ResponseEntity<?> getToBePublishDiagramsByUserId(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize) {

        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<ToBePublishedDiagramDto> toBePublishedDiagrams = publicDiagramService
                .getToBePublishedDiagrams(authUser.userId(), pageable);
        return ResponseEntity.ok(new PageResponse<>(toBePublishedDiagrams));
    }

    @GetMapping("/view/{diagramId}")
    public ResponseEntity<?> getPublicDiagram(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable UUID diagramId) {

        PublicDiagramDto publicDiagramDto = publicDiagramService.viewPublicDiagram(authUser.userId(), diagramId);
        return ResponseEntity.ok(publicDiagramDto);
    }

    @PostMapping("/fork/{diagramId}")
    public ResponseEntity<?> forkPublicDiagram(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable UUID diagramId) {

        publicDiagramService.forkPublicDiagram(authUser.userId(), diagramId);
        return ResponseEntity.ok("Diagram forked successfully :)");
    }

    @PostMapping("/star/{diagramId}")
    public ResponseEntity<?> starPublicDiagram(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable UUID diagramId) {

        publicDiagramService.starPublicDiagram(authUser.userId(), diagramId);
        return ResponseEntity.ok("Diagram stared successfully :)");
    }

    @DeleteMapping("/unstar/{diagramId}")
    public ResponseEntity<?> unstarPublicDiagram(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable UUID diagramId) {

        publicDiagramService.unstarPublicDiagram(authUser.userId(), diagramId);
        return ResponseEntity.ok("Diagram stared successfully :)");
    }

    @GetMapping("/forkedDiagrams")
    public ResponseEntity<?> getForkedDiagram(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize) {

        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<PublicDiagramInfoDto> forkedDiagrams = publicDiagramService.getForkedPublicDiagrams(authUser.userId(),
                pageable);
        return ResponseEntity.ok(new PageResponse<>(forkedDiagrams));
    }

    @PostMapping("/staredDiagrams")
    public ResponseEntity<?> getStaredDiagram(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize) {

        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<PublicDiagramInfoDto> staredDiagrams = publicDiagramService.getStaredPublicDiagrams(authUser.userId(),
                pageable);
        return ResponseEntity.ok(new PageResponse<>(staredDiagrams));
    }

    @PostMapping("/searchDiagrams")
    public ResponseEntity<?> searchPublicDiagram(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestBody SearchRequestDto searchRequestDto) {

        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<PublicDiagramInfoDto> publicDiagramsInfos = publicDiagramService.searchPublicDiagrams(searchRequestDto,
                pageable);
        return ResponseEntity.ok(new PageResponse<>(publicDiagramsInfos));
    }

    @PostMapping("/searchUsers")
    public ResponseEntity<?> searchPublicUsers(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestBody SearchRequestDto searchRequestDto) {

        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<PublicUserInfoDto> publicUserInfos = publicDiagramService.searchUsersByPublicDiagrams(authUser.userId(), searchRequestDto,
                pageable);
        return ResponseEntity.ok(new PageResponse<>(publicUserInfos));
    }

    @GetMapping("/hashtags")
    public ResponseEntity<?> getHashtags(
            @AuthenticationPrincipal AuthUser authUser) {

        List<String> hashtags = hashtagService.getAll();
        return ResponseEntity.ok(hashtags);
    }

    @DeleteMapping("/unpublish/{diagramId}")
    public ResponseEntity<?> unPublishPublicDiagram(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable @NonNull UUID diagramId) {

        publicDiagramService.unPublishPublicDiagram(authUser.userId(), diagramId);
        return ResponseEntity.ok("The Public Diagram has been deleted successfully :)");
    }

}
