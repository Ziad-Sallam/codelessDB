package backend.databaseManagement;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CreateDatabaseDTO {
    private String databaseName;
    private String databasePassword;
    private int serverId;
    private String ddl;
}
