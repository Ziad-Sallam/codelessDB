package backend.databaseManagement.dto;

import java.util.*;

import backend.databaseManagement.Database;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SendDatabasesDTO {
    private List<Database> databases = new ArrayList<>();

}
