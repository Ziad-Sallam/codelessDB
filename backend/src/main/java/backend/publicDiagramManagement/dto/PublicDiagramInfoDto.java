package backend.publicDiagramManagement.dto;

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
public class PublicDiagramInfoDto {
    private UUID diagramId;
    private String name;
    private String thumbnail;
    private Date createdAt;
    private Date lastModified;
    private List<ContributorDto> contributors;

    private String shortDescription;
    private List<String> hashTags;

    private int stars;
    private int forks;
    private int views;
}
