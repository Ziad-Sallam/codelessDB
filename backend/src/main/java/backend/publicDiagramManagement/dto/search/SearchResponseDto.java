package backend.publicDiagramManagement.dto.search;

import org.springframework.data.domain.Page;

import backend.publicDiagramManagement.dto.PublicDiagramInfoDto;
import backend.publicDiagramManagement.dto.user.PublicUserInfoDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response object containing search results for users and public diagrams")
public class SearchResponseDto {
    @Schema(description = "A paginated list of users matching the search criteria")
    Page<PublicUserInfoDto> users;

    @Schema(description = "A paginated list of public diagrams matching the search criteria")
    Page<PublicDiagramInfoDto> publicDiagrams;
}
