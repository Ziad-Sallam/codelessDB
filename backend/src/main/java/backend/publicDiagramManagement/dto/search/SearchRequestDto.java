package backend.publicDiagramManagement.dto.search;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Request object for searching public diagrams and users")
public class SearchRequestDto {
    @Schema(description = "The search query or prompt", example = "E-commerce")
    String searchPrompt;

    @Schema(description = "List of hashtags to filter by", example = "[\"sql\", \"database\"]")
    List<String> hashtags;
}
