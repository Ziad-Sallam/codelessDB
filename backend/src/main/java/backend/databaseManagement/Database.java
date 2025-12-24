package backend.databaseManagement;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Schema(description = "Data Transfer Object representing a database instance")
public class Database {
    @Schema(description = "The unique identifier of the database", example = "1")
    private Integer databaseId;
    @Schema(description = "The name of the database", example = "ecommerce_db")
    private String databaseName;
    @Schema(description = "The name of the server where the database resides", example = "Production Server")
    private String serverName;
    @Schema(description = "The DDL (Data Definition Language) of the database", example = "CREATE TABLE ...")
    private String databaseddl;
    @Schema(description = "Whether the database is currently connected and accessible", example = "true")
    private boolean isConnected;

}