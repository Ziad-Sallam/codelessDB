package backend.SQLOptimization.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import backend.SQLOptimization.dto.OptimizeSQLRequest;
import backend.SQLOptimization.dto.OptimizeSQLResponse;
import backend.SQLOptimization.service.SQLOptimizationService;
import backend.security.AuthUser;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SQLOptimizationController {

    private final SQLOptimizationService optimizationService;

    @PostMapping("/optimize-sql")
    public ResponseEntity<OptimizeSQLResponse> optimizeSQL(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody OptimizeSQLRequest request) {
        
        OptimizeSQLResponse response = optimizationService.optimizeSQL(request.getSqlCode());
        return ResponseEntity.ok(response);
    }
}
