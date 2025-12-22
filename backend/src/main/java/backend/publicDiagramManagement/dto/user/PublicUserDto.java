package backend.publicDiagramManagement.dto.user;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


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
    @JsonProperty("isFollowed")
    private boolean isFollowed;
    private int followersCount;
    private int followingCount;
    private long starredCount;
}
