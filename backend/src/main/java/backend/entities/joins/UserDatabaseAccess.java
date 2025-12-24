package backend.entities.joins;

import backend.entities.User;
import backend.entities.UserDatabase;
import backend.user.Role;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_database_access")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDatabaseAccess {

    @Builder.Default
    @EmbeddedId
    private UserDatabaseAccessId id = new UserDatabaseAccessId();

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @MapsId("databaseId")
    @JoinColumn(name = "database_id", nullable = false)
    private UserDatabase database;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
}
