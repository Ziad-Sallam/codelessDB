package backend.databaseManagement;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Database{
    private Integer databaseId;
    private String databaseName;
    private String serverName;

}