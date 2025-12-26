package backend.user;

import backend.entities.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchDto {
    private int id;
    private String username;
    private String email;
    private String picture;
    private String bio;

    public UserSearchDto(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.picture = user.getPicture();
        this.bio = user.getBio();
    }
}
