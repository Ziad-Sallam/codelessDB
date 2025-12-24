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
@Schema(description = "Data Transfer Object for initiating a database container")
public class InitiateDatabaseDTO {
    @Schema(description = "The name of the database", example = "ecommerce_db")
    private String databaseName;
    @Schema(description = "Password for the database container", example = "secret_pass")
    private String password;
    @Schema(description = "The DDL (Data Definition Language) for the database", example = "CREATE TABLE ...")
    private String ddl;
    @Schema(description = "The WebSocket URL for the communication agent", example = "ws://localhost:8080/...")
    private String wsUrl;
    @Schema(description = "The name of the Docker container", example = "mysql_container_1")
    private String containerName;
    @Schema(description = "The unique identifier of the container", example = "101")
    private int containerId;

}
