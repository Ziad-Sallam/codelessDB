package backend.databaseManagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Getter
public class PasswordRequest {
    private int databaseId;
    private String password;
}
