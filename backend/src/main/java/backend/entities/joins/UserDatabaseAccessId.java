package backend.entities.joins;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDatabaseAccessId implements Serializable {
    @Column(name = "user_id")
    private int userId;

    @Column(name = "database_id")
    private int databaseId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserDatabaseAccessId)) return false;
        UserDatabaseAccessId that = (UserDatabaseAccessId) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(databaseId, that.databaseId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, databaseId);
    }
}
