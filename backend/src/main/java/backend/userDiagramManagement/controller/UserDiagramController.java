package backend.userDiagramManagement.controller;

import java.time.LocalDateTime;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import backend.publicDiagramManagement.dto.PageResponse;
import backend.security.AuthUser;
import backend.config.ErrorResponse;
import backend.userDiagramManagement.dto.DiagramDto;
import backend.userDiagramManagement.dto.DiagramInfoDto;
import backend.userDiagramManagement.dto.create.DiagramCreateRequestDto;
import backend.userDiagramManagement.dto.delete.DiagramDeleteResponseDto;
import backend.userDiagramManagement.dto.search.DiagramSearchRequestDto;
import backend.userDiagramManagement.dto.share.DiagramShareRequestDto;
import backend.userDiagramManagement.dto.share.DiagramShareResponseDto;
import backend.userDiagramManagement.dto.update.DiagramUpdateRequestDto;
import backend.userDiagramManagement.dto.update.DiagramUpdateResponseDto;
import backend.userDiagramManagement.service.IUserDiagramService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/diagrams")
@RequiredArgsConstructor
@Tag(name = "User Diagram Management", description = "Endpoints for creating, managing, and sharing private database diagrams")
public class UserDiagramController {

    private final IUserDiagramService userDiagramService;

    private int id(AuthUser authUser) {
        return authUser.userId();
    }

    @GetMapping("/get")
    @Operation(summary = "Get user diagrams", description = "Lists all diagrams owned by the authenticated user with pagination")
    @ApiResponse(responseCode = "200", description = "Returns paginated list of diagrams", 
                 content = @Content(schema = @Schema(implementation = PageResponse.class)))
    public ResponseEntity<?> getDiagramsByUserId(
            @AuthenticationPrincipal AuthUser authUser,
            @Parameter(description = "Zero-indexed page number") @RequestParam(defaultValue = "0") int pageNumber,
            @Parameter(description = "Number of items per page") @RequestParam(defaultValue = "10") int pageSize) {

        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<DiagramInfoDto> result = userDiagramService.getDiagramsByUserId(id(authUser), pageable);

        return ResponseEntity.ok(new PageResponse<>(result));
    }

    @PostMapping("/create")
    @Operation(summary = "Create new diagram", description = "Initializes a new database diagram for the user")
    @ApiResponse(responseCode = "200", description = "Diagram created successfully")
    public ResponseEntity<DiagramInfoDto> createDiagram(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody DiagramCreateRequestDto request) {

        DiagramInfoDto diagram = userDiagramService.createDiagram(id(authUser), request);

        return ResponseEntity.ok(diagram);
    }

    	@PutMapping("/update/{id}")
	@Operation(summary = "Update diagram", description = "Updates an existing diagram's content, structure, and metadata")
	@ApiResponse(responseCode = "200", description = "Diagram updated successfully with timestamp")
	@ApiResponse(responseCode = "403", description = "Forbidden Action", 
	             content = @Content(schema = @Schema(implementation = ErrorResponse.class),
	             examples = @ExampleObject(name = "Access Denied", value = "{\"message\": \"You do not have permission to perform this action on the specified diagram\", \"status\": 403}")))
	@ApiResponse(responseCode = "404", description = "Diagram not found", 
	             content = @Content(schema = @Schema(implementation = ErrorResponse.class),
	             examples = @ExampleObject(name = "Not Found", value = "{\"message\": \"The specified diagram ID could not be found\", \"status\": 404}")))
	public ResponseEntity<DiagramUpdateResponseDto> updateDiagram(
			@AuthenticationPrincipal AuthUser authUser,
			@Parameter(description = "UUID of the diagram to update") @PathVariable UUID id,
			@Parameter(description = "Updated diagram data including JSON content") @RequestBody DiagramUpdateRequestDto request) {

        LocalDateTime updateDate = userDiagramService.updateDiagram(id(authUser), request, id);

        return ResponseEntity.ok(new DiagramUpdateResponseDto(
                "Diagram updated successfully",
                id,
                updateDate));
    }

    	@DeleteMapping("/delete/{id}")
	@Operation(summary = "Delete diagram", description = "Permanently deletes a diagram. Only the owner can delete their diagrams.")
	@ApiResponse(responseCode = "200", description = "Diagram deleted successfully")
	@ApiResponse(responseCode = "403", description = "Forbidden Action", 
	             content = @Content(schema = @Schema(implementation = ErrorResponse.class),
	             examples = @ExampleObject(name = "Access Denied", value = "{\"message\": \"You do not have permission to perform this action on the specified diagram\", \"status\": 403}")))
	@ApiResponse(responseCode = "404", description = "Diagram not found", 
	             content = @Content(schema = @Schema(implementation = ErrorResponse.class),
	             examples = @ExampleObject(name = "Not Found", value = "{\"message\": \"The specified diagram ID could not be found\", \"status\": 404}")))
	public ResponseEntity<DiagramDeleteResponseDto> deleteDiagram(
			@AuthenticationPrincipal AuthUser authUser,
			@Parameter(description = "UUID of the diagram to delete") @PathVariable UUID id) {

        userDiagramService.deleteDiagram(id(authUser), id);

        return ResponseEntity.ok(new DiagramDeleteResponseDto(
                "Diagram deleted successfully",
                id));
    }

    @PostMapping("/search")
	@Operation(summary = "Search diagrams", description = "Searches user's diagrams by title, description, or tags with pagination support")
	@ApiResponse(responseCode = "200", description = "Returns paginated search results", 
                 content = @Content(schema = @Schema(implementation = PageResponse.class)))
	public ResponseEntity<?> searchDiagrams(
			@AuthenticationPrincipal AuthUser authUser,
			@Parameter(description = "Zero-indexed page number") @RequestParam int pageNumber,
			@Parameter(description = "Number of items per page") @RequestParam int pageSize,
			@Parameter(description = "Search criteria including query string and filters") @RequestBody DiagramSearchRequestDto request) {

        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<DiagramInfoDto> result = userDiagramService.searchDiagrams(id(authUser), request, pageable);

        return ResponseEntity.ok(new PageResponse<>(result));
    }

    	@GetMapping("/search/{id}")
	@Operation(summary = "Get diagram by ID", description = "Retrieves complete diagram details including JSON content, metadata, and sharing information")
	@ApiResponse(responseCode = "200", description = "Returns full diagram data")
	@ApiResponse(responseCode = "403", description = "Forbidden Action", 
	             content = @Content(schema = @Schema(implementation = ErrorResponse.class),
	             examples = @ExampleObject(name = "Access Denied", value = "{\"message\": \"You do not have permission to perform this action on the specified diagram\", \"status\": 403}")))
	@ApiResponse(responseCode = "404", description = "Diagram not found", 
	             content = @Content(schema = @Schema(implementation = ErrorResponse.class),
	             examples = @ExampleObject(name = "Not Found", value = "{\"message\": \"The specified diagram ID could not be found\", \"status\": 404}")))
	public ResponseEntity<DiagramDto> searchDiagramById(
			@AuthenticationPrincipal AuthUser authUser,
			@Parameter(description = "UUID of the diagram") @PathVariable UUID id) {

        DiagramDto diagram = userDiagramService.searchDiagramById(id(authUser), id);
        return ResponseEntity.ok(diagram);
    }

    	@PutMapping("/share/{id}")
	@Operation(summary = "Share diagram with collaborators", description = "Grants other users access to view or edit a diagram. Returns updated list of contributors.")
	@ApiResponse(responseCode = "200", description = "Diagram shared successfully with list of current contributors")
	@ApiResponse(responseCode = "403", description = "Forbidden Action", 
	             content = @Content(schema = @Schema(implementation = ErrorResponse.class),
	             examples = @ExampleObject(name = "Access Denied", value = "{\"message\": \"You do not have permission to perform this action on the specified diagram\", \"status\": 403}")))
	@ApiResponse(responseCode = "404", description = "Diagram not found", 
	             content = @Content(schema = @Schema(implementation = ErrorResponse.class),
	             examples = @ExampleObject(name = "Not Found", value = "{\"message\": \"The specified diagram ID could not be found\", \"status\": 404}")))
	public ResponseEntity<DiagramShareResponseDto> shareDiagram(
			@AuthenticationPrincipal AuthUser authUser,
			@Parameter(description = "UUID of the diagram to share") @PathVariable UUID id,
			@Parameter(description = "List of usernames to grant access to") @RequestBody DiagramShareRequestDto request) {

        DiagramShareResponseDto response = userDiagramService.shareDiagram(id(authUser), id, request);
        return ResponseEntity.ok(response);
    }

    	@PutMapping("/update-ddl/{id}")
	@Operation(summary = "Update diagram DDL", description = "Updates the Data Definition Language (DDL) SQL script for the diagram")
	@ApiResponse(responseCode = "200", description = "DDL updated successfully")
	@ApiResponse(responseCode = "403", description = "Forbidden Action", 
	             content = @Content(schema = @Schema(implementation = ErrorResponse.class),
	             examples = @ExampleObject(name = "Access Denied", value = "{\"message\": \"You do not have permission to perform this action on the specified diagram\", \"status\": 403}")))
	@ApiResponse(responseCode = "404", description = "Diagram not found", 
	             content = @Content(schema = @Schema(implementation = ErrorResponse.class),
	             examples = @ExampleObject(name = "Not Found", value = "{\"message\": \"The specified diagram ID could not be found\", \"status\": 404}")))
	public ResponseEntity<String> updateDDL(
			@AuthenticationPrincipal AuthUser authUser,
			@Parameter(description = "UUID of the diagram") @PathVariable UUID id,
			@Parameter(description = "DDL SQL script content") @RequestBody String ddl) {

        userDiagramService.updateDDL(id, ddl);

        return ResponseEntity.ok("DDL updated successfully");
    }
}
