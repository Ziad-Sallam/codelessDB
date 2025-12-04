package backend.publicDiagramManagement.controller;

import backend.publicDiagramManagement.service.PublicDiagramService;
import backend.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@Controller
@RequestMapping("/publicDiagrams")
@RequiredArgsConstructor
public class PublicDiagramController {

    private final PublicDiagramService publicDiagramService;

    @PostMapping("/publish")
    public ResponseEntity<?> searchDiagrams(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable UUID diagramID) {


        return ResponseEntity.ok(authUser);
    }
}
