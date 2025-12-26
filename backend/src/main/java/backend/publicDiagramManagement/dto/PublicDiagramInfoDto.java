package backend.publicDiagramManagement.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;

import backend.entities.publicDiagramEntities.Hashtag;
import backend.entities.publicDiagramEntities.PublicDiagram;
import backend.userDiagramManagement.dto.ContributorDto;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Summary Data Transfer Object for public diagram listings")
public class PublicDiagramInfoDto {
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

    @ArraySchema(
            schema = @Schema(
                    implementation = ContributorDto.class,
                    description = "List of contributors to the diagram"
            )
    )
    private List<ContributorDto> contributors;

    @Schema(description = "A short description of the diagram", example = "A simple schema for an e-commerce platform")
    private String shortDescription;

    @ArraySchema(
            schema = @Schema(
                    description = "Hashtag associated with the diagram",
                    example = "database"
            )
    )
    private List<String> hashTags;

    @Schema(description = "Number of stars the diagram has received", example = "10")
    private int stars;

    @Schema(description = "Number of times the diagram has been forked", example = "3")
    private int forks;

    @Schema(description = "Number of views the diagram has received", example = "100")
    private int views;

    public static PublicDiagramInfoDto toDto(
            PublicDiagram publicDiagram,
            List<ContributorDto> contributors) {

        return PublicDiagramInfoDto.builder()
                .diagramId(publicDiagram.getId())

                // Diagram fields (already LocalDateTime)
                .name(publicDiagram.getDiagram().getName())
                .thumbnail(publicDiagram.getDiagram().getThumbnail())
                .createdAt(publicDiagram.getDiagram().getCreatedAt())
                .lastModified(publicDiagram.getDiagram().getLastModified())

                .contributors(contributors)

                // Public diagram fields
                .shortDescription(publicDiagram.getShortDescription())

                .hashTags(
                        publicDiagram.getHashtags()
                                .stream()
                                .map(Hashtag::getName)
                                .toList())

                .stars(publicDiagram.getStars())
                .forks(publicDiagram.getForks())
                .views(publicDiagram.getViews())

                .build();
    }

}
