package backend.publicDiagramManagement.controller;

import java.util.List;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import backend.config.ErrorResponse;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/publicDiagrams")
@RequiredArgsConstructor
@Tag(name = "Public Diagram Management", description = "Endpoints for sharing, searching, and interacting with public database diagrams")
public class PublicDiagramController {

    private final PublicDiagramService publicDiagramService;
    private final HashtagService hashtagService;

    @PostMapping("/publish")
    @Operation(summary = "Publish a diagram", description = "Shares a private diagram to the public gallery")
    @ApiResponse(responseCode = "200", description = "Diagram published successfully")
    public ResponseEntity<?> publishDiagram(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody PublishDiagramRequestDto publicDiagramDto) {

        publicDiagramService.publishDiagram(authUser.userId(), publicDiagramDto);
        return ResponseEntity.ok("Diagram published successfully :)");
    }

    @org.springframework.web.bind.annotation.PutMapping("/update")
    @Operation(summary = "Update published diagram", description = "Updates metadata of an already published diagram including title, description, and hashtags")
    @ApiResponse(responseCode = "200", description = "Diagram details updated successfully")
    @ApiResponse(responseCode = "403", description = "User does not own this published diagram", 
                 content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                 examples = @ExampleObject(name = "Forbidden", value = "{\"message\": \"You do not have permission to modify this public diagram\", \"status\": 403}")))
    @ApiResponse(responseCode = "404", description = "Published diagram not found", 
                 content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                 examples = @ExampleObject(name = "Not Found", value = "{\"message\": \"The specified public diagram could not be found\", \"status\": 404}")))
    public ResponseEntity<?> updateDiagram(
            @AuthenticationPrincipal AuthUser authUser,
            @Parameter(description = "Updated diagram publication details") @RequestBody PublishDiagramRequestDto publicDiagramDto) {

        publicDiagramService.updatePublicDiagram(authUser.userId(), publicDiagramDto);
        return ResponseEntity.ok("Diagram details updated successfully :)");
    }

    @GetMapping("/getToBePublished")
    @Operation(summary = "Get diagrams eligible for publishing", description = "Retrieves list of user's private diagrams that can be published to the public gallery")
    @ApiResponse(responseCode = "200", description = "Returns paginated list of publishable diagrams", 
                 content = @Content(schema = @Schema(implementation = PageResponse.class)))
    public ResponseEntity<?> getToBePublishDiagramsByUserId(
            @AuthenticationPrincipal AuthUser authUser,
            @Parameter(description = "Zero-indexed page number") @RequestParam(defaultValue = "0") int pageNumber,
            @Parameter(description = "Number of items per page") @RequestParam(defaultValue = "10") int pageSize) {

        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<ToBePublishedDiagramDto> toBePublishedDiagrams = publicDiagramService
                .getToBePublishedDiagrams(authUser.userId(), pageable);
        return ResponseEntity.ok(new PageResponse<>(toBePublishedDiagrams));
    }

    @GetMapping("/view/{diagramId}")
    @Operation(summary = "View public diagram details", description = "Retrieves full details of a specific public diagram")
    @ApiResponse(responseCode = "200", description = "Returns the diagram data", 
                 content = @Content(schema = @Schema(implementation = PublicDiagramDto.class)))
    @ApiResponse(responseCode = "404", description = "Diagram not found", 
                 content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                 examples = @ExampleObject(value = "{\"message\": \"The public diagram ID does not exist\", \"status\": 404}")))
    public ResponseEntity<?> getPublicDiagram(
            @AuthenticationPrincipal AuthUser authUser,
            @Parameter(description = "UUID of the diagram") @PathVariable UUID diagramId) {

        PublicDiagramDto publicDiagramDto = publicDiagramService.viewPublicDiagram(authUser.userId(), diagramId);
        return ResponseEntity.ok(publicDiagramDto);
    }

    @PostMapping("/fork/{diagramId}")
    @Operation(summary = "Fork a public diagram", description = "Creates a copy of a public diagram in the user's private workspace for modification")
    @ApiResponse(responseCode = "200", description = "Diagram forked successfully")
    @ApiResponse(responseCode = "404", description = "Public diagram not found", 
                 content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                 examples = @ExampleObject(value = "{\"message\": \"Diagram not found in the public gallery\", \"status\": 404}")))
    public ResponseEntity<?> forkPublicDiagram(
            @AuthenticationPrincipal AuthUser authUser,
            @Parameter(description = "UUID of the public diagram to fork") @PathVariable UUID diagramId) {

        publicDiagramService.forkPublicDiagram(authUser.userId(), diagramId);
        return ResponseEntity.ok("Diagram forked successfully :)");
    }

    @PostMapping("/star/{diagramId}")
    @Operation(summary = "Star a public diagram", description = "Adds the diagram to user's starred collection and increments the diagram's star count")
    @ApiResponse(responseCode = "200", description = "Diagram starred successfully")
    @ApiResponse(responseCode = "404", description = "Public diagram not found", 
                 content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                 examples = @ExampleObject(value = "{\"message\": \"Diagram not found in the public gallery\", \"status\": 404}")))
    public ResponseEntity<?> starPublicDiagram(
            @AuthenticationPrincipal AuthUser authUser,
            @Parameter(description = "UUID of the diagram to star") @PathVariable UUID diagramId) {

        publicDiagramService.starPublicDiagram(authUser.userId(), diagramId);
        return ResponseEntity.ok("Diagram stared successfully :)");
    }

    @DeleteMapping("/unstar/{diagramId}")
    @Operation(summary = "Unstar a public diagram", description = "Removes the diagram from user's starred collection and decrements the star count")
    @ApiResponse(responseCode = "200", description = "Diagram unstarred successfully")
    @ApiResponse(responseCode = "404", description = "Public diagram not found", 
                 content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                 examples = @ExampleObject(value = "{\"message\": \"Diagram not found in the public gallery\", \"status\": 404}")))
    public ResponseEntity<?> unstarPublicDiagram(
            @AuthenticationPrincipal AuthUser authUser,
            @Parameter(description = "UUID of the diagram to unstar") @PathVariable UUID diagramId) {

        publicDiagramService.unstarPublicDiagram(authUser.userId(), diagramId);
        return ResponseEntity.ok("Diagram stared successfully :)");
    }

    @GetMapping("/forkedDiagrams")
    @Operation(summary = "Get user's forked diagrams", description = "Retrieves all public diagrams that the user has forked into their workspace")
    @ApiResponse(responseCode = "200", description = "Returns paginated list of forked diagrams", 
                 content = @Content(schema = @Schema(implementation = PageResponse.class)))
    public ResponseEntity<?> getForkedDiagram(
            @AuthenticationPrincipal AuthUser authUser,
            @Parameter(description = "Zero-indexed page number") @RequestParam(defaultValue = "0") int pageNumber,
            @Parameter(description = "Number of items per page") @RequestParam(defaultValue = "10") int pageSize) {

        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<PublicDiagramInfoDto> forkedDiagrams = publicDiagramService.getForkedPublicDiagrams(authUser.userId(),
                pageable);
        return ResponseEntity.ok(new PageResponse<>(forkedDiagrams));
    }

    @PostMapping("/staredDiagrams")
    @Operation(summary = "Get user's starred diagrams", description = "Retrieves all public diagrams that the user has starred")
    @ApiResponse(responseCode = "200", description = "Returns paginated list of starred diagrams", 
                 content = @Content(schema = @Schema(implementation = PageResponse.class)))
    public ResponseEntity<?> getStaredDiagram(
            @AuthenticationPrincipal AuthUser authUser,
            @Parameter(description = "Zero-indexed page number") @RequestParam(defaultValue = "0") int pageNumber,
            @Parameter(description = "Number of items per page") @RequestParam(defaultValue = "10") int pageSize) {

        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<PublicDiagramInfoDto> staredDiagrams = publicDiagramService.getStaredPublicDiagrams(authUser.userId(),
                pageable);
        return ResponseEntity.ok(new PageResponse<>(staredDiagrams));
    }

    @PostMapping("/searchDiagrams")
    @Operation(summary = "Search public diagrams", description = "Searches the public gallery by keywords, hashtags, and filters with pagination")
    @ApiResponse(responseCode = "200", description = "Returns paginated search results", 
                 content = @Content(schema = @Schema(implementation = PageResponse.class)))
    public ResponseEntity<?> searchPublicDiagram(
            @AuthenticationPrincipal AuthUser authUser,
            @Parameter(description = "Zero-indexed page number") @RequestParam(defaultValue = "0") int pageNumber,
            @Parameter(description = "Number of items per page") @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "Search criteria including keywords and hashtags") @RequestBody SearchRequestDto searchRequestDto) {

        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<PublicDiagramInfoDto> publicDiagramsInfos = publicDiagramService.searchPublicDiagrams(searchRequestDto,
                pageable);
        return ResponseEntity.ok(new PageResponse<>(publicDiagramsInfos));
    }

    @PostMapping("/searchUsers")
    @Operation(summary = "Search users with public diagrams", description = "Searches for users who have published diagrams, filtered by username or profile info")
    @ApiResponse(responseCode = "200", description = "Returns paginated list of users", 
                 content = @Content(schema = @Schema(implementation = PageResponse.class)))
    public ResponseEntity<?> searchPublicUsers(
            @AuthenticationPrincipal AuthUser authUser,
            @Parameter(description = "Zero-indexed page number") @RequestParam(defaultValue = "0") int pageNumber,
            @Parameter(description = "Number of items per page") @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "Search criteria for finding users") @RequestBody SearchRequestDto searchRequestDto) {

        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<PublicUserInfoDto> publicUserInfos = publicDiagramService.searchUsersByPublicDiagrams(authUser.userId(), searchRequestDto,
                pageable);
        return ResponseEntity.ok(new PageResponse<>(publicUserInfos));
    }

    @GetMapping("/hashtags")
    @Operation(summary = "Get all hashtags", description = "Retrieves list of all available hashtags used in public diagrams for filtering")
    @ApiResponse(responseCode = "200", description = "Returns list of hashtag strings")
    public ResponseEntity<?> getHashtags(
            @AuthenticationPrincipal AuthUser authUser) {

        List<String> hashtags = hashtagService.getAll();
        return ResponseEntity.ok(hashtags);
    }

    @DeleteMapping("/unpublish/{diagramId}")
    @Operation(summary = "Unpublish a diagram", description = "Removes a diagram from the public gallery and makes it private again")
    @ApiResponse(responseCode = "200", description = "Diagram unpublished successfully")
    @ApiResponse(responseCode = "403", description = "User does not own this published diagram", 
                 content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                 examples = @ExampleObject(name = "Forbidden", value = "{\"message\": \"You do not have permission to modify this public diagram\", \"status\": 403}")))
    @ApiResponse(responseCode = "404", description = "Published diagram not found", 
                 content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                 examples = @ExampleObject(name = "Not Found", value = "{\"message\": \"The specified public diagram could not be found\", \"status\": 404}")))
    public ResponseEntity<?> unPublishPublicDiagram(
            @AuthenticationPrincipal AuthUser authUser,
            @Parameter(description = "UUID of the diagram to unpublish") @PathVariable @NonNull UUID diagramId) {

        publicDiagramService.unPublishPublicDiagram(authUser.userId(), diagramId);
        return ResponseEntity.ok("The Public Diagram has been deleted successfully :)");
    }

}
