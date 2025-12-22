package backend.publicDiagramManagement.dto.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchRequestDto {
    String searchPrompt;
    List<String> hashtags;
}
