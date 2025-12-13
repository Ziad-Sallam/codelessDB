package backend.publicDiagramManagement.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;

import backend.entities.publicDiagramEntities.Hashtag;
import backend.entities.publicDiagramEntities.PublicDiagram;
import backend.userDiagramManagement.dto.ContributorDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PublicDiagramInfoDto {

    private UUID diagramId;
    private String name;
    private String thumbnail;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastModified;

    private List<ContributorDto> contributors;

    private String shortDescription;
    private List<String> hashTags;

    private int stars;
    private int forks;
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
