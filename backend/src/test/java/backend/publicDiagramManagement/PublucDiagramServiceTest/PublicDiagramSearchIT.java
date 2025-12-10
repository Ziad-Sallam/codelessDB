package backend.publicDiagramManagement.PublucDiagramServiceTest;

import backend.entities.Diagram;
import backend.entities.User;
import backend.entities.joins.UserDiagram;
import backend.entities.publicDiagramEntities.Hashtag;
import backend.entities.publicDiagramEntities.PublicDiagram;
import backend.publicDiagramManagement.repository.HashtagRepository;
import backend.publicDiagramManagement.repository.PublicDiagramRepository;
import backend.user.UserRepository;
import backend.userDiagramManagement.repository.DiagramRepository;
import backend.userDiagramManagement.repository.UserDiagramRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Transactional
public class PublicDiagramSearchIT {

    @Autowired private PublicDiagramRepository publicDiagramRepository;
    @Autowired private HashtagRepository hashtagRepository;
    @Autowired private DiagramRepository diagramRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private UserDiagramRepository userDiagramRepository;

    private final List<String> TAG_NAMES = List.of("java", "uml", "backend", "design");

    private List<Hashtag> hashtags;
    private User owner;

    private final Random rnd = new Random(42);

    @BeforeEach
    void seedDatabase() {

        // ---- create user ----
        owner = User.builder()
                .username("tester")
                .email("tester@example.com")
                .password("pass")
                .bio("bio java backend")
                .picture("pic.png")
                .profileWebsiteUrl("http://example.com")
                .build();
        userRepository.save(owner);

        // ---- save hashtags ----
        hashtags = new ArrayList<>();
        for (String t : TAG_NAMES) {
            hashtags.add(hashtagRepository.save(Hashtag.builder().name(t).build()));
        }

        // ---- create 10 diagrams ----
        for (int i = 1; i <= 10; i++) {

            Diagram d = Diagram.builder()
                    .name("Diagram " + i)
                    .ddl("CREATE TABLE t" + i + "(id INT);")
                    .content("{}")
                    .thumbnail(null)
                    .build();

            d = diagramRepository.save(d);

            // attach diagram owner
            userDiagramRepository.save(
                    UserDiagram.builder()
                            .user(owner)
                            .diagram(d)
                            .role(backend.user.Role.OWNER)
                            .build()
            );

            PublicDiagram pd = PublicDiagram.builder()
                    .diagram(d) // REQUIRED because MapsId
                    .shortDescription("Short desc " + i)
                    .detailedDescription(i % 2 == 0 ?
                            "This explains java and backend" :
                            "UML design description")
                    .stars(rnd.nextInt(10))
                    .forks(rnd.nextInt(5))
                    .views(rnd.nextInt(200))
                    .hashtags(new HashSet<>())
                    .build();

            // assign tags
            pd.getHashtags().add(hashtags.get(i % hashtags.size()));
            if (i % 2 == 0) {
                pd.getHashtags().add(hashtags.get((i + 1) % hashtags.size()));
            }

            publicDiagramRepository.save(pd);
        }

        publicDiagramRepository.flush();
    }

    // ==========================================================
    // Test 1 — keyword
    // ==========================================================

    @Test
    @DisplayName("Search by keyword returns matching public diagrams")
    void searchByKeyword() {

        Page<PublicDiagram> page =
                publicDiagramRepository.searchPublicDiagrams(
                        "java",
                        Collections.emptyList(),
                        PageRequest.of(0, 20)
                );

        assertThat(page.getTotalElements()).isGreaterThan(0);

        page.forEach(pd ->
                assertThat(pd.getDetailedDescription().toLowerCase())
                        .contains("java")
        );
    }

    // ==========================================================
    // Test 2 — hashtag filtering
    // ==========================================================

    @Test
    @DisplayName("Search by hashtag returns only diagrams containing that tag")
    void searchByTag() {

        Page<PublicDiagram> page =
                publicDiagramRepository.searchPublicDiagrams(
                        null,
                        List.of("uml"),
                        PageRequest.of(0, 20)
                );

        assertThat(page.getTotalElements()).isGreaterThan(0);

        page.forEach(pd -> {
            boolean match = pd.getHashtags()
                    .stream()
                    .anyMatch(h -> h.getName().equals("uml"));
            assertThat(match).isTrue();
        });
    }

    // ==========================================================
    // Test 3 — combined filters
    // ==========================================================

    @Test
    @DisplayName("Combined search (keyword + tag)")
    void combinedSearch() {

        Page<PublicDiagram> page =
                publicDiagramRepository.searchPublicDiagrams(
                        "design",
                        List.of("backend"),
                        PageRequest.of(0, 50)
                );

        page.forEach(pd -> {
            boolean text =
                    pd.getShortDescription().toLowerCase().contains("design")
                            || pd.getDetailedDescription().toLowerCase().contains("design");

            boolean tag =
                    pd.getHashtags().stream()
                            .anyMatch(h -> h.getName().equals("backend"));

            assertThat(text || tag).isTrue();
        });
    }

    // ==========================================================
    // Test 4 — empty search returns all
    // ==========================================================

    @Test
    @DisplayName("Empty search returns all diagrams")
    void emptySearchReturnsAll() {

        Page<PublicDiagram> page =
                publicDiagramRepository.searchPublicDiagrams(
                        "",
                        Collections.emptyList(),
                        PageRequest.of(0, 50)
                );

        assertThat(page.getTotalElements()).isEqualTo(10);
    }

    // ==========================================================
    // Test 5 — ordering by score
    // ==========================================================

    @Test
    @DisplayName("Results ordered by score desc")
    void orderingByScore() {

        Page<PublicDiagram> page =
                publicDiagramRepository.searchPublicDiagrams(
                        null,
                        Collections.emptyList(),
                        PageRequest.of(0, 20)
                );

        List<PublicDiagram> list = page.getContent();

        for (int i = 1; i < list.size(); i++) {
            long prev = list.get(i - 1).getViews()
                    + list.get(i - 1).getForks() * 2L
                    + list.get(i - 1).getStars() * 3L;

            long curr = list.get(i).getViews()
                    + list.get(i).getForks() * 2L
                    + list.get(i).getStars() * 3L;

            assertThat(prev).isGreaterThanOrEqualTo(curr);
        }
    }
}
