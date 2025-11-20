package backend.userDiagramManagement.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class DiagramDto {
    private UUID id;
    private String name;
    private String content;
    private byte[] thumbnail;
    private LocalDateTime createdAt;
    private LocalDateTime lastModified;
    private String role;
}

