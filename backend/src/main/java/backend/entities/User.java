package backend.entities;

import java.sql.Date;
import java.util.*;

import org.hibernate.annotations.CreationTimestamp;

import backend.entities.joins.UserDiagram;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, unique = true, length = 50)
    @NotBlank(message = "Username is mandatory")
    @Size(min = 3, max = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    @NotBlank(message = "Email is mandatory")
    @Email
    private String email;

    @Column(nullable = false, length = 60)
    private String password;

    @Column(length = 255)
    private String picture;

    @Column(length = 500)
    private String bio;

    @Column(length = 255)
    private String profileWebsiteUrl;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private Date createdAt;

    /* ------------------------------ FOLLOWERS & FOLLOWING ------------------------------ */

    /** Users who follow THIS user */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_followers",
            joinColumns = @JoinColumn(name = "user_id"),              // this user
            inverseJoinColumns = @JoinColumn(name = "follower_id")    // users who follow
    )
    private Set<User> followers = new HashSet<>();

    /** Users THIS user follows */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_following",
            joinColumns = @JoinColumn(name = "follower_id"),          // this user
            inverseJoinColumns = @JoinColumn(name = "user_id")        // users being followed
    )
    private Set<User> following = new HashSet<>();

    /* ------------------------------ DIAGRAM RELATIONSHIP ------------------------------ */

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserDiagram> userDiagrams = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_servers",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "server_id")
    )
    private Set<Server> servers = new HashSet<>();

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserDatabase> ownedDatabases = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_database_access",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "database_id")
    )
    private Set<UserDatabase> accessibleDatabases = new HashSet<>();
}
