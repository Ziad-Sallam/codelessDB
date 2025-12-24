package backend.databaseManagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Schema(description = "Data Transfer Object for server configuration")
public class CreateServerDTO {
    @Schema(description = "The name of the server", example = "Production Server")
    private String serverName;

    @Schema(description = "The unique identifier of the server", example = "1")
    private int serverId;
}
