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
public class AddDatabaseToUser {
    private int databaseId;
    private int userId;
    private int ownerId;
    private String role;
}
