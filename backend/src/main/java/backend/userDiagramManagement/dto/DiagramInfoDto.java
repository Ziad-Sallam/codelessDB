package backend.userDiagramManagement.dto;

import backend.entities.Diagram;
import backend.user.Role;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiagramInfoDto {
    private UUID diagramId;
    private String name;
    private String thumbnail;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastModified;

    private Role role;
    private List<ContributorDto> contributorDtos;

    private boolean isPublic;

    public static DiagramInfoDto toDto(Diagram diagram, Role role, List<ContributorDto> contributorDtos,  boolean isPublic) {
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
