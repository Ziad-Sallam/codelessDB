package backend.publicDiagramManagement.controller;

import backend.publicDiagramManagement.dto.comment.CommentDto;
import backend.publicDiagramManagement.dto.comment.CommentRequestDto;
import backend.publicDiagramManagement.service.CommentService;
import backend.security.AuthUser;
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
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/{diagramId}/comments")
    public ResponseEntity<List<CommentDto>> getComments(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable UUID diagramId) {

        int userId = (authUser != null) ? authUser.userId() : 0;
        return ResponseEntity.ok(commentService.getComments(userId, diagramId));
    }

    @PostMapping("/{diagramId}/comments")
    public ResponseEntity<CommentDto> addComment(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable UUID diagramId,
            @RequestBody @Valid CommentRequestDto commentRequestDto) {

        return ResponseEntity.ok(commentService.addComment(authUser.userId(), diagramId, commentRequestDto));
    }

    @PutMapping("/comments/{commentId}")
    public ResponseEntity<CommentDto> updateComment(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long commentId,
            @RequestBody @Valid CommentRequestDto commentRequestDto) {

        return ResponseEntity.ok(commentService.updateComment(authUser.userId(), commentId, commentRequestDto));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long commentId) {

        commentService.deleteComment(authUser.userId(), commentId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/comments/{commentId}/react")
    public ResponseEntity<CommentDto> reactToComment(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long commentId,
            @RequestParam String type) {

        return ResponseEntity.ok(commentService.reactToComment(authUser.userId(), commentId, type));
    }
}
