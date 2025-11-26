package backend.databaseManagement;


import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class InitiateDatabaseDTO {
    private String databaseName;
    private String password;
    private String ddl;
    private String wsUrl;
    private String containerName;
    private int containerId;
    
}
