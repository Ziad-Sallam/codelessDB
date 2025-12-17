package backend.agent.HTTPHandler;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
public class MessageDTO {
        private int databaseId;
        private String content;

}
