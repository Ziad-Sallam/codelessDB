package backend.publicDiagramManagement.dto.get;

import backend.userDiagramManagement.dto.ContributorDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ToBePublishedDiagramDto {
    private UUID diagramId;
    private String name;
    private String thumbnail;
    private Date createdAt;
    private Date lastModified;
    private String ddl;
    private List<ContributorDto> contributors;
}
