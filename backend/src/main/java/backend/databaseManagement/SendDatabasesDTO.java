package backend.databaseManagement;

import java.util.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SendDatabasesDTO {
    private List<Database> databases = new ArrayList<>();

}
