package backend.entities;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_database")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDatabase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = true)
    private Server server;

    @OneToMany(mappedBy = "database", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CannedQueriesDB> queries = new ArrayList<>();


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToMany(mappedBy = "accessibleDatabases")
    private List<User> usersWithAccess = new ArrayList<>();

    @Column(nullable = false)
    private String password;

    @Column(columnDefinition = "LONGTEXT")
    private String ddl;
}
