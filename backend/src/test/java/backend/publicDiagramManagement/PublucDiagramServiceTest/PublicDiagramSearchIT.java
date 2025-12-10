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

import java.time.LocalDateTime;
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
    private List<Hashtag> savedTags;
    private User testUser;

    @BeforeEach
    void seedDatabase() {
        // create and save a user (fill required fields)
        testUser = User.builder()
                .username("tester")
                .email("tester@example.com")
                .password("secret")
                .bio("test user")
                .picture("pic.png")
                .profileWebsiteUrl("http://example.com")
                .build();
        userRepository.save(testUser);

        // save hashtags first (avoid transient errors)
        savedTags = new ArrayList<>();
        for (String t : TAG_NAMES) {
            Hashtag h = Hashtag.builder().name(t).build();
            savedTags.add(hashtagRepository.save(h));
        }

        Random rnd = new Random(123);

        // create 10 diagrams + public diagrams
        for (int i = 1; i <= 10; i++) {
            // create and persist Diagram (must be persisted before PublicDiagram)
            Diagram diagram = Diagram.builder()
                    .name("Diagram " + i)
                    .ddl("CREATE TABLE t" + i + " (id INT);")
                    .content("{}")
                    .thumbnail(null)
                    .build();
            diagram = diagramRepository.save(diagram); // gives diagram an id (UUID)

            // attach diagram to a user (owner) to reflect real relations
            UserDiagram ud = UserDiagram.builder()
                    .user(testUser)
                    .diagram(diagram)
                    .role(backend.user.Role.OWNER)
                    .build();
            userDiagramRepository.save(ud);

            // build public diagram WITHOUT setting id manually.
            // because PublicDiagram uses @MapsId, Hibernate will take the id from diagram.
            PublicDiagram pd = PublicDiagram.builder()
                    .diagram(diagram) // <--- required (persisted)
                    .shortDescription("Short desc " + i)
                    .detailedDescription(i % 2 == 0
                            ? "This explains java and backend concepts"
                            : "UML modeling example and design notes")
                    .stars(rnd.nextInt(10))
                    .forks(rnd.nextInt(5))
                    .views(rnd.nextInt(200))
                    .hashtags(new HashSet<>())
                    .build();

            // attach a subset of saved hashtags
            pd.getHashtags().add(savedTags.get(i % savedTags.size()));
            if (i % 2 == 0) pd.getHashtags().add(savedTags.get((i + 1) % savedTags.size()));

            publicDiagramRepository.save(pd);
        }

        // ensure DB flush so tests see stable state
        publicDiagramRepository.flush();
    }

    @Test
    @DisplayName("Search by keyword in detailedDescription")
    void searchByKeyword_shouldReturnMatches() {
        Page<PublicDiagram> page = publicDiagramRepository.searchPublicDiagrams(
                "java",
                Collections.emptyList(),
                PageRequest.of(0, 20)
        );

        assertThat(page.getTotalElements()).isGreaterThan(0);
        page.forEach(pd -> assertThat(pd.getDetailedDescription().toLowerCase()).contains("java"));
    }

    @Test
    @DisplayName("Search by single hashtag returns only matched")
    void searchByHashtag_shouldReturnMatches() {
        Page<PublicDiagram> page = publicDiagramRepository.searchPublicDiagrams(
                null,
                List.of("uml"),
                PageRequest.of(0, 20)
        );

        assertThat(page.getTotalElements()).isGreaterThan(0);
        page.forEach(pd -> assertThat(
                pd.getHashtags().stream().anyMatch(h -> "uml".equals(h.getName()))
        ).isTrue());
    }

    @Test
    @DisplayName("Combined search (keyword + tags)")
    void combinedSearch_shouldFilterCorrectly() {
        Page<PublicDiagram> page = publicDiagramRepository.searchPublicDiagrams(
                "design",
                List.of("backend"),
                PageRequest.of(0, 50)
        );

        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(0);
        // if there are any results validate they match filter criteria
        page.forEach(pd -> {
            boolean textMatch = pd.getShortDescription().toLowerCase().contains("design")
                    || (pd.getDetailedDescription() != null && pd.getDetailedDescription().toLowerCase().contains("design"));
            boolean tagMatch = pd.getHashtags().stream().anyMatch(h -> "backend".equals(h.getName()));
            assertThat(textMatch || tagMatch).isTrue();
        });
    }

    @Test
    @DisplayName("Empty search returns all seeded rows")
    void emptySearch_returnsAll() {
        Page<PublicDiagram> page = publicDiagramRepository.searchPublicDiagrams(
                "",
                Collections.emptyList(),
                PageRequest.of(0, 50)
        );

        assertThat(page.getTotalElements()).isEqualTo(10);
    }

    @Test
    @DisplayName("Results are ordered by score (views + forks*2 + stars*3)")
    void resultsOrdering_shouldBeByScoreDesc() {
        Page<PublicDiagram> page = publicDiagramRepository.searchPublicDiagrams(
                null,
                Collections.emptyList(),
                PageRequest.of(0, 20)
        );

        List<PublicDiagram> list = page.getContent();
        assertThat(list.size()).isGreaterThan(1);

        for (int i = 1; i < list.size(); i++) {
            long sPrev = list.get(i - 1).getViews() + list.get(i - 1).getForks() * 2L + list.get(i - 1).getStars() * 3L;
            long sCurr = list.get(i).getViews() + list.get(i).getForks() * 2L + list.get(i).getStars() * 3L;
            assertThat(sPrev).isGreaterThanOrEqualTo(sCurr);
        }
    }
}
