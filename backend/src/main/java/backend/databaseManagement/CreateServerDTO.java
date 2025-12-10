package backend.databaseManagement;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CreateServerDTO {
    private String serverName;
    private int serverId;
    
}
