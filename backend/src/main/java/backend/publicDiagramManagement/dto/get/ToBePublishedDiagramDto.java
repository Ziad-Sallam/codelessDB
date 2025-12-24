package backend.publicDiagramManagement.dto.get;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;

import backend.userDiagramManagement.dto.ContributorDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Data Transfer Object for a diagram that is ready to be published")
public class ToBePublishedDiagramDto {
    @Schema(description = "The unique ID of the diagram", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID diagramId;

    @Schema(description = "The name of the diagram", example = "E-commerce Schema")
    private String name;

    @Schema(description = "URL or base64 of the diagram thumbnail", example = "https://example.com/thumbnail.png")
    private String thumbnail;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "The timestamp when the diagram was created")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "The timestamp when the diagram was last modified")
    private LocalDateTime lastModified;

    @Schema(description = "The DDL (Data Definition Language) of the diagram", example = "CREATE TABLE users...")
    private String ddl;

    @Schema(description = "List of contributors to the diagram")
    private List<ContributorDto> contributors;
}
