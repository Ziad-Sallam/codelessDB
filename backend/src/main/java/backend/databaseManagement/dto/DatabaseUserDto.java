package backend.databaseManagement.dto;

import backend.entities.joins.UserDatabaseAccess;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DatabaseUserDto {
    private int userId;
    private String username;
    private String email;
    private String picture;
    private String role;

    public DatabaseUserDto(UserDatabaseAccess access) {
        this.userId = access.getUser().getId();
        this.username = access.getUser().getUsername();
        this.email = access.getUser().getEmail();
        this.picture = access.getUser().getPicture();
        this.role = access.getRole().toString();
    }
}
