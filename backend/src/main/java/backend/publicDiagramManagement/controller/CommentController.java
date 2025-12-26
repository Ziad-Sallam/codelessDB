package backend.publicDiagramManagement.controller;

import backend.publicDiagramManagement.dto.comment.CommentDto;
import backend.publicDiagramManagement.dto.comment.CommentRequestDto;
import backend.config.ErrorResponse;
import backend.publicDiagramManagement.service.CommentService;
import backend.security.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/publicDiagrams")
@RequiredArgsConstructor
@Tag(name = "Comments & Social Interaction", description = "Endpoints for managing comments and reactions on public diagrams")
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "Get all comments for a public diagram", description = "Retrieves a list of all comments associated with a specific public diagram. Includes reaction counts.")
    @GetMapping("/{diagramId}/comments")
    public ResponseEntity<List<CommentDto>> getComments(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable UUID diagramId) {

        int userId = (authUser != null) ? authUser.userId() : 0;
        return ResponseEntity.ok(commentService.getComments(userId, diagramId));
    }

    @PostMapping("/{diagramId}/comments")
    @Operation(summary = "Add a comment", description = "Adds a new comment to a public diagram")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Comment added successfully")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Diagram not found", 
                                                         content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<CommentDto> addComment(
            @AuthenticationPrincipal AuthUser authUser,
            @io.swagger.v3.oas.annotations.Parameter(description = "UUID of the diagram") @PathVariable UUID diagramId,
            @io.swagger.v3.oas.annotations.Parameter(description = "Comment content") @RequestBody @Valid CommentRequestDto commentRequestDto) {

        return ResponseEntity.ok(commentService.addComment(authUser.userId(), diagramId, commentRequestDto));
    }

    @PutMapping("/comments/{commentId}")
    @Operation(summary = "Update a comment", description = "Updates an existing comment. Only the comment author can update.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Comment updated successfully")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Not authorized to update this comment", 
                                                         content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Comment not found", 
                                                         content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<CommentDto> updateComment(
            @AuthenticationPrincipal AuthUser authUser,
            @io.swagger.v3.oas.annotations.Parameter(description = "ID of the comment to update") @PathVariable Long commentId,
            @io.swagger.v3.oas.annotations.Parameter(description = "Updated comment content") @RequestBody @Valid CommentRequestDto commentRequestDto) {

        return ResponseEntity.ok(commentService.updateComment(authUser.userId(), commentId, commentRequestDto));
    }

    @DeleteMapping("/comments/{commentId}")
    @Operation(summary = "Delete a comment", description = "Deletes a comment. Only the comment author can delete.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Comment deleted successfully")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Not authorized to delete this comment", 
                                                         content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Comment not found", 
                                                         content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<Void> deleteComment(
            @AuthenticationPrincipal AuthUser authUser,
            @io.swagger.v3.oas.annotations.Parameter(description = "ID of the comment to delete") @PathVariable Long commentId) {

        commentService.deleteComment(authUser.userId(), commentId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/comments/{commentId}/react")
    @Operation(summary = "React to a comment", description = "Adds or updates a reaction (like, love, etc.) to a comment")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reaction added successfully")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Comment not found", 
                                                         content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<CommentDto> reactToComment(
            @AuthenticationPrincipal AuthUser authUser,
            @io.swagger.v3.oas.annotations.Parameter(description = "ID of the comment") @PathVariable Long commentId,
            @io.swagger.v3.oas.annotations.Parameter(description = "Reaction type (e.g., 'like', 'love')") @RequestParam String type) {

        return ResponseEntity.ok(commentService.reactToComment(authUser.userId(), commentId, type));
    }
}
