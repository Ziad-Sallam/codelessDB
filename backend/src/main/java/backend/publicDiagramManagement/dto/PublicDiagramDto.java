package backend.publicDiagramManagement.dto;

import backend.entities.Diagram;
import backend.entities.publicDiagramEntities.Hashtag;
import backend.entities.publicDiagramEntities.PublicDiagram;
import backend.userDiagramManagement.dto.ContributorDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PublicDiagramDto {
    private UUID diagramId;
    private String name;
    private String thumbnail;
    private Date createdAt;
    private Date lastModified;
    private List<ContributorDto> contributors;

    private String shortDescription;
    private String detailedDescription;
    private String ddl;
    private List<String> hashTags;
    private List<CannedQueryDto> cannedQueries;

    private int stars;
    private int forks;
    private int views;

    public static PublicDiagramDto toDto(Diagram diagram, PublicDiagram publicDiagram, List<ContributorDto> contributorDtos) {
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
                .cannedQueries(publicDiagram.getCannedQueries().stream().map(CannedQueryDto::toDto).toList())
                .stars(publicDiagram.getStars())
                .forks(publicDiagram.getForks())
                .views(publicDiagram.getViews())
                .build();
    }
}
