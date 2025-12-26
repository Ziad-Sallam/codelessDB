package backend.publicDiagramManagement.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import backend.publicDiagramManagement.dto.PageResponse;
import backend.publicDiagramManagement.dto.PublicDiagramInfoDto;
import backend.publicDiagramManagement.dto.user.PublicUserDto;
import backend.config.ErrorResponse;
import backend.publicDiagramManagement.dto.user.PublicUserFollowDto;
import backend.publicDiagramManagement.service.PublicUserService;
import backend.security.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/publicUsers")
@RequiredArgsConstructor
@Tag(name = "Public User Profiles", description = "Endpoints for viewing designer profiles, followers, and social interactions")
public class PublicUserController {

    private final PublicUserService publicUserService;

    @GetMapping("/designerProfile/{userName}")
    @Operation(summary = "Get designer profile", description = "Retrieves public profile information (bio, stats, etc.) for a specific user")
    @ApiResponse(responseCode = "200", description = "Returns public user profile", 
                 content = @Content(schema = @Schema(implementation = PublicUserDto.class)))
    @ApiResponse(responseCode = "404", description = "User not found", 
                 content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<?> getDesignerProfile(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable @NonNull String userName) {

        PublicUserDto publicUserDto = publicUserService.getDesignerProfile(authUser != null ? authUser.userId() : null, userName);

        return ResponseEntity.ok(publicUserDto);
    }

    @GetMapping("/publicDiagrams/{userName}")
    @Operation(summary = "Get user's public diagrams", description = "Retrieves all diagrams published by a specific user with pagination")
    @ApiResponse(responseCode = "200", description = "Returns paginated list of diagrams", 
                 content = @Content(schema = @Schema(implementation = PageResponse.class)))
    @ApiResponse(responseCode = "404", description = "User not found", 
                 content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<?> getPublicDiagrams(
            @AuthenticationPrincipal AuthUser authUser,
            @io.swagger.v3.oas.annotations.Parameter(description = "Zero-indexed page number") @RequestParam(defaultValue = "0") int pageNumber,
            @io.swagger.v3.oas.annotations.Parameter(description = "Number of items per page") @RequestParam(defaultValue = "10") int pageSize,
            @io.swagger.v3.oas.annotations.Parameter(description = "Username of the designer") @PathVariable @NonNull String userName) {

        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<PublicDiagramInfoDto> publicUserDto = publicUserService.getPublicDiagrams(userName, pageable);

        return ResponseEntity.ok(new PageResponse<>(publicUserDto));
    }

    @GetMapping("/staredDiagrams/{userName}")
    @Operation(summary = "Get user's starred diagrams", description = "Retrieves all diagrams that a specific user has starred")
    @ApiResponse(responseCode = "200", description = "Returns paginated list of starred diagrams", 
                 content = @Content(schema = @Schema(implementation = PageResponse.class)))
    @ApiResponse(responseCode = "404", description = "User not found", 
                 content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<?> getStaredDiagram(
            @AuthenticationPrincipal AuthUser authUser,
            @io.swagger.v3.oas.annotations.Parameter(description = "Zero-indexed page number") @RequestParam(defaultValue = "0") int pageNumber,
            @io.swagger.v3.oas.annotations.Parameter(description = "Number of items per page") @RequestParam(defaultValue = "10") int pageSize,
            @io.swagger.v3.oas.annotations.Parameter(description = "Username of the user") @PathVariable @NonNull String userName) {

        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<PublicDiagramInfoDto> staredDiagrams = publicUserService.getStaredPublicDiagrams(userName, pageable);
        return ResponseEntity.ok(new PageResponse<>(staredDiagrams));
    }

    @PostMapping("/follow/{userName}")
    @Operation(summary = "Follow a user", description = "Adds the specified user to the authenticated user's following list")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User followed successfully")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found", 
                                                         content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<?> followUser(
            @AuthenticationPrincipal AuthUser authUser,
            @io.swagger.v3.oas.annotations.Parameter(description = "Username to follow") @PathVariable @NonNull String userName) {

        publicUserService.followUser(authUser.userId(), userName);
        return ResponseEntity.ok( userName + " is followed successfully :)");
    }

    @DeleteMapping("/unfollow/{userName}")
    @Operation(summary = "Unfollow a user", description = "Removes the specified user from the authenticated user's following list")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User unfollowed successfully")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found", 
                                                         content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<?> unfollowUser(
            @AuthenticationPrincipal AuthUser authUser,
            @io.swagger.v3.oas.annotations.Parameter(description = "Username to unfollow") @PathVariable @NonNull String userName) {

        publicUserService.unfollowUser(authUser.userId(), userName);
        return ResponseEntity.ok( userName + " is unfollowed successfully :)");
    }

    @GetMapping("/followers/{userName}")
    @Operation(summary = "Get user's followers", description = "Retrieves paginated list of users following the specified user")
    @ApiResponse(responseCode = "200", description = "Returns paginated list of followers", 
                 content = @Content(schema = @Schema(implementation = PageResponse.class)))
    @ApiResponse(responseCode = "404", description = "User not found", 
                 content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<?> getFollowers (
            @io.swagger.v3.oas.annotations.Parameter(description = "Username to get followers for") @PathVariable @NonNull String userName,
            @io.swagger.v3.oas.annotations.Parameter(description = "Zero-indexed page number") @RequestParam(defaultValue = "0") int pageNumber,
            @io.swagger.v3.oas.annotations.Parameter(description = "Number of items per page") @RequestParam(defaultValue = "10") int pageSize) {

        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<PublicUserFollowDto> followers = publicUserService.getFollowersByUsername(userName, pageable);
        return ResponseEntity.ok(new PageResponse<>(followers));
    }

    @GetMapping("/followings/{userName}")
    @Operation(summary = "Get user's followings", description = "Retrieves paginated list of users that the specified user is following")
    @ApiResponse(responseCode = "200", description = "Returns paginated list of followings", 
                 content = @Content(schema = @Schema(implementation = PageResponse.class)))
    @ApiResponse(responseCode = "404", description = "User not found", 
                 content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<?> getFollowings (
            @io.swagger.v3.oas.annotations.Parameter(description = "Username to get followings for") @PathVariable @NonNull String userName,
            @io.swagger.v3.oas.annotations.Parameter(description = "Zero-indexed page number") @RequestParam(defaultValue = "0") int pageNumber,
            @io.swagger.v3.oas.annotations.Parameter(description = "Number of items per page") @RequestParam(defaultValue = "10") int pageSize) {

        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<PublicUserFollowDto> followings = publicUserService.getFollowingsByUsername(userName, pageable);
        return ResponseEntity.ok(new PageResponse<>(followings));
    }
}
