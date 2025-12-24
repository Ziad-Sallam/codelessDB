package backend.userDiagramManagement.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;

import backend.entities.Diagram;
import backend.user.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Summary Data Transfer Object for a database diagram")
public class DiagramInfoDto {
    @Schema(description = "The unique identifier of the diagram", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID diagramId;
    @Schema(description = "The name of the diagram", example = "E-commerce System")
    private String name;
    @Schema(description = "URL or base64 of the diagram thumbnail", example = "https://example.com/thumbnail.png")
    private String thumbnail;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "The timestamp when the diagram was created")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "The timestamp when the diagram was last modified")
    private LocalDateTime lastModified;

    @Schema(description = "The role of the current user for this diagram")
    private Role role;
    @Schema(description = "List of contributors to this diagram")
    private List<ContributorDto> contributorDtos;

    @Schema(description = "Whether the diagram is published to the public gallery", example = "true")
    private boolean isPublic;

    public static DiagramInfoDto toDto(Diagram diagram, Role role, List<ContributorDto> contributorDtos,
            boolean isPublic) {
        return DiagramInfoDto
                .builder()
                .role(role)
                .diagramId(diagram.getId())
                .name(diagram.getName())
                .thumbnail(diagram.getThumbnail())
                .createdAt(diagram.getCreatedAt())
                .lastModified(diagram.getLastModified())
                .contributorDtos(contributorDtos)
                .isPublic(isPublic)
                .build();
    }
}
