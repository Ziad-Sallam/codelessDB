package backend.databaseManagement.dto;

import backend.databaseManagement.Database;
import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Data Transfer Object for sending a list of user databases")
public class SendDatabasesDTO {
    @Schema(description = "List of databases owned by the user", implementation = Database.class)
    private List<Database> databases = new ArrayList<>();

}
