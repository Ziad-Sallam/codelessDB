package backend.databaseManagement.dto;

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
public class InitiateDatabaseDTO {
    private String databaseName;
    private String ddl;
    private String wsUrl;
    private String containerName;
    private int containerId;

}
