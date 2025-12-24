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
import backend.config.ErrorResponse;
import backend.security.AuthUser;
import backend.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "SQL Optimization", description = "AI-powered SQL query optimization and performance suggestions")
public class SQLOptimizationController {

    private final SQLOptimizationService optimizationService;
    private final UserService userService;

    @PostMapping("/optimize-sql")
    @Operation(summary = "Optimize SQL query", description = "Uses AI to analyze and optimize SQL queries, providing performance suggestions and improvements. Consumes AI quota.")
    @ApiResponse(responseCode = "200", description = "SQL optimized successfully with suggestions",
            content = @Content(schema = @Schema(implementation = OptimizeSQLResponse.class)))
    @ApiResponse(responseCode = "429", description = "AI quota exceeded", 
                 content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                 examples = @ExampleObject(name = "Quota Exceeded", 
                                          value = "{\"message\": \"AI optimization quota exceeded. Please try again tomorrow.\", \"status\": 429}")))
    public ResponseEntity<OptimizeSQLResponse> optimizeSQL(
            @AuthenticationPrincipal AuthUser authUser,
            @Parameter(description = "SQL code to optimize") @RequestBody OptimizeSQLRequest request) {

        // Check and decrement AI quota before processing
        userService.checkAndDecrementAiQuota(authUser.userId());

        OptimizeSQLResponse response = optimizationService.optimizeSQL(request.getSqlCode());
        return ResponseEntity.ok(response);
    }
}
