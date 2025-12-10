package backend.publicDiagramManagement.dto.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicUserDto {
    private String name;
    private String username;
    private String bio;
    private String picture;
    private String email;
    private String url;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime createdAt;
    private long publicCount;
    private long totalStars;
}
