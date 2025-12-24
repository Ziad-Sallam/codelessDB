package backend.SQLOptimization.controller;

import jakarta.validation.Valid;
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

    @PostMapping(value = "/optimize-sql", consumes = "application/json", produces = "application/json")
    @Operation(
            summary = "Optimize SQL query",
            description = "Uses AI to analyze and optimize SQL queries, providing performance suggestions and improvements. Consumes AI quota."
    )
    @ApiResponse(
            responseCode = "200",
            description = "SQL optimized successfully with suggestions",
            content = @Content(schema = @Schema(implementation = OptimizeSQLResponse.class))
    )
    @ApiResponse(
            responseCode = "429",
            description = "AI quota exceeded",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                            name = "Quota Exceeded",
                            value = "{\"message\": \"AI optimization quota exceeded. Please try again tomorrow.\", \"status\": 429}"
                    )
            )
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "SQL code to optimize",
            required = true,
            content = @Content(
                    schema = @Schema(implementation = OptimizeSQLRequest.class),
                    examples = @ExampleObject(value = "{ \"sqlCode\": \"SELECT * FROM users\" }")
            )
    )
    public ResponseEntity<OptimizeSQLResponse> optimizeSQL(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody OptimizeSQLRequest request) {

        userService.checkAndDecrementAiQuota(authUser.userId());

        OptimizeSQLResponse response = optimizationService.optimizeSQL(request.getSqlCode());
        return ResponseEntity.ok(response);
    }
}

