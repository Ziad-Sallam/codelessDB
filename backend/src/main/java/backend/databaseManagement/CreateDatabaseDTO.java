package backend.databaseManagement;

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
@Schema(description = "Data Transfer Object for creating a database")
public class CreateDatabaseDTO {
    @Schema(description = "The unique identifier of the database", example = "1")
    private Integer databaseId;

    @Schema(description = "The name of the database", example = "ecommerce_db")
    private String databaseName;

    @Schema(description = "Password for the database", example = "secret_pass")
    private String databasePassword;

    @Schema(description = "The ID of the server where the database resides", example = "1")
    private Integer serverId;

    @Schema(description = "The DDL (Data Definition Language) for the database", example = "CREATE TABLE users (...)")
    private String ddl;

    @Schema(description = "The name of the server", example = "Production Server")
    private String serverName;
}
