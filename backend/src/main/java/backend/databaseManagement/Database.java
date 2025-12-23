package backend.databaseManagement;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Database {
    private Integer databaseId;
    private String databaseName;
    private String serverName;
    private String databaseddl;
    private boolean isConnected;
    private String role;

}