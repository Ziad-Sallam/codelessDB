package backend.publicDiagramManagement.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;

import backend.entities.Diagram;
import backend.entities.publicDiagramEntities.Hashtag;
import backend.entities.publicDiagramEntities.PublicDiagram;
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
@Schema(description = "Detailed Data Transfer Object for a public diagram")
public class PublicDiagramDto {
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

    @Schema(description = "List of contributors to the diagram", implementation = ContributorDto.class)
    private List<ContributorDto> contributors;

    @Schema(description = "A short description of the diagram", example = "A simple schema for an e-commerce platform")
    private String shortDescription;

    @Schema(description = "A detailed description of the diagram", example = "This schema includes tables for users, orders, and products...")
    private String detailedDescription;

    @Schema(description = "The DDL (Data Definition Language) of the diagram", example = "CREATE TABLE users...")
    private String ddl;

    @Schema(description = "List of hashtags associated with the diagram")
    private List<String> hashTags;

    @Schema(description = "List of canned queries associated with the diagram", implementation = DiagramCannedQueryDto.class)
    private List<DiagramCannedQueryDto> cannedQueries;

    @Schema(description = "Number of stars the diagram has received", example = "10")
    private int stars;

    @Schema(description = "Number of times the diagram has been forked", example = "3")
    private int forks;

    @Schema(description = "Number of views the diagram has received", example = "100")
    private int views;

    @Schema(description = "Whether the current user has starred the diagram", example = "true")
    private boolean isStared;

    public static PublicDiagramDto toDto(Diagram diagram, PublicDiagram publicDiagram,
            List<ContributorDto> contributorDtos, boolean isStared) {
        return PublicDiagramDto.builder()
                .diagramId(diagram.getId())
                .name(diagram.getName())
                .thumbnail(diagram.getThumbnail())
                .createdAt(diagram.getCreatedAt())
                .lastModified(diagram.getLastModified())
                .contributors(contributorDtos)
                .shortDescription(publicDiagram.getShortDescription())
                .detailedDescription(publicDiagram.getDetailedDescription())
                .ddl(diagram.getDdl())
                .hashTags(publicDiagram.getHashtags().stream().map(Hashtag::getName).toList())
                .cannedQueries(publicDiagram.getCannedQueries().stream().map(DiagramCannedQueryDto::toDto).toList())
                .stars(publicDiagram.getStars())
                .forks(publicDiagram.getForks())
                .views(publicDiagram.getViews())
                .isStared(isStared)
                .build();
    }
}
