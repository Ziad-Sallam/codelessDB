package backend.collab.updateDto;

import lombok.Data;

@Data
public class DiagramUpdateRequestDto {
    private String name;
    private byte[] jsonContent;
    private String thumbnail;
}
