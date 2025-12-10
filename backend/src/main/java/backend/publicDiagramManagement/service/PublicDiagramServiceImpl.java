package backend.publicDiagramManagement.service;

import backend.entities.Diagram;
import backend.entities.User;
import backend.entities.joins.PublicDiagramUserId;
import backend.entities.joins.UserDiagram;
import backend.entities.publicDiagramEntities.*;
import backend.publicDiagramManagement.dto.PublicDiagramDto;
import backend.publicDiagramManagement.dto.PublicDiagramInfoDto;
import backend.publicDiagramManagement.dto.get.ToBePublishedDiagramDto;
import backend.publicDiagramManagement.dto.publish.PublishDiagramRequestDto;
import backend.publicDiagramManagement.dto.search.SearchRequestDto;
import backend.publicDiagramManagement.dto.user.PublicUserInfoDto;
import backend.publicDiagramManagement.exceptions.PublicDiagramException;
import backend.publicDiagramManagement.repository.ForkRepository;
import backend.publicDiagramManagement.repository.PublicDiagramRepository;
import backend.publicDiagramManagement.repository.StarRepository;
import backend.publicDiagramManagement.repository.ViewsRepository;
import backend.user.Role;
import backend.user.UserRepository;
import backend.user.UserService;
import backend.userDiagramManagement.dto.ContributorDto;
import backend.userDiagramManagement.dto.DiagramInfoDto;
import backend.userDiagramManagement.repository.DiagramRepository;
import backend.userDiagramManagement.repository.UserDiagramRepository;
import backend.userDiagramManagement.service.UserDiagramService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PublicDiagramServiceImpl implements PublicDiagramService {

    private final UserDiagramRepository userDiagramRepository;
    private final DiagramRepository diagramRepository;
    private final PublicDiagramRepository publicDiagramRepository;
    private final UserDiagramService userDiagramService;
    private final HashtagService hashtagService;
    private final ViewsService viewsService;
    private final ViewsRepository viewsRepository;
    private final ForkRepository forkRepository;
    private final StarRepository starRepository;
    private final UserService userService;
    private final UserRepository userRepository;

    public PublicDiagram getPublicDiagramOrThrow(Diagram diagram) {
        PublicDiagram pd = diagram.getPublicDiagram();
        if (pd == null) {
            throw new PublicDiagramException.DiagramNotFoundException(
                    "Public diagram not found " + diagram.getId()
            );
        }
        return pd;
    }

    @Override
    @Transactional
    public void publishDiagram(int userId, PublishDiagramRequestDto dto) {

        UserDiagram userDiagram =
                userDiagramService.getUserDiagramOrThrow(userId, dto.getDiagramId());

        userDiagramService.checkOwner(userDiagram, "publish");

        Diagram diagram = userDiagram.getDiagram();

        PublicDiagram publicDiagram =
                publicDiagramRepository.findById(diagram.getId())
                        .orElseGet(() -> {
                            PublicDiagram pd = new PublicDiagram();
                            pd.setDiagram(diagram);
                            pd.setId(diagram.getId());
                            pd.setStars(0);
                            pd.setForks(0);
                            pd.setViews(0);
                            return pd;
                        });

        // Public metadata only
        publicDiagram.setShortDescription(dto.getShortDescription());
        publicDiagram.setDetailedDescription(dto.getDetailedDescription());

        // Hashtags
        Set<Hashtag> hashtags =
                hashtagService.resolveHashtags(new HashSet<>(dto.getHashTags()));
        publicDiagram.setHashtags(hashtags);

        // Canned Queries
        Set<CannedQueriesDiagrams> cannedQueries =
                dto.getCannedQueries().stream()
                        .map(q -> {
                            CannedQueriesDiagrams e = new CannedQueriesDiagrams();
                            e.setName(q.getName());
                            e.setDescription(q.getDescription());
                            e.setQuery(q.getQuery());
                            e.setPublicDiagram(publicDiagram);
                            return e;
                        })
                        .collect(Collectors.toSet());

        if (publicDiagram.getCannedQueries() == null) {
            publicDiagram.setCannedQueries(new HashSet<>());
        }

        publicDiagram.getCannedQueries().clear();
        publicDiagram.getCannedQueries().addAll(cannedQueries);

        publicDiagramRepository.save(publicDiagram);
    }


    @Transactional
    public PublicDiagramDto viewPublicDiagram(int userId, UUID diagramId) {

        Diagram diagram = userDiagramService.getDiagramOrThrow(diagramId);
        PublicDiagram publicDiagram = getPublicDiagramOrThrow(diagram);

        boolean newView = viewsService.addView(userId, diagramId);

        if (newView) {
            publicDiagram.setViews(publicDiagram.getViews() + 1);
        }

        List<ContributorDto> contributors = userDiagramService.getContributors(diagramId);

        return PublicDiagramDto.toDto(diagram, publicDiagram, contributors);
    }

    @Override
    @Transactional
    public void forkPublicDiagram(int userId, UUID diagramId) {

        Diagram original = userDiagramService.getDiagramOrThrow(diagramId);
        PublicDiagram publicDiagram = getPublicDiagramOrThrow(original);

        // Clone diagram
        Diagram cloned = Diagram.builder()
                .name(original.getName())
                .ddl(original.getDdl())
                .content(original.getContent())
                .thumbnail(original.getThumbnail())
                .build();
        diagramRepository.save(cloned);

        // Attach clone to user
        UserDiagram ud = UserDiagram.builder()
                .user(userService.getUserOrThrow(userId))
                .diagram(cloned)
                .role(Role.OWNER)
                .build();
        userDiagramRepository.save(ud);

        // Save fork record
        PublicDiagramUserId forkId = PublicDiagramUserId.builder()
                .userId(userId)
                .publicDiagramId(publicDiagram.getId())
                .build();

        DiagramFork fork = DiagramFork.builder()
                .id(forkId)
                .originalDiagram(publicDiagram)
                .build();

        forkRepository.save(fork);

        publicDiagramRepository.incrementForks(publicDiagram.getId());
    }

    @Override
    public Page<ToBePublishedDiagramDto> getToBePublishedDiagrams(int userId, Pageable pageable) {

        Page<Diagram> diagrams =
                userDiagramRepository.findUnpublishedDiagramsByUser(userId, pageable);

        return diagrams.map(d -> ToBePublishedDiagramDto.builder()
                .diagramId(d.getId())
                .name(d.getName())
                .thumbnail(d.getThumbnail())
                .createdAt(d.getCreatedAt())
                .lastModified(d.getLastModified())
                .ddl(d.getDdl())
                .contributors(userDiagramService.getContributors(d.getId()))
                .build()
        );
    }

    @Override
    public Page<PublicDiagramInfoDto> getForkedPublicDiagrams(int userId, Pageable pageable) {

        Page<PublicDiagram> forkedDiagrams = forkRepository.findForkedPublicDiagramsByUser(userId, pageable);

        return forkedDiagrams.map(publicDiagram -> {
            List<ContributorDto> contributors = userDiagramService.getContributors(publicDiagram.getId());
            return PublicDiagramInfoDto.toDto(publicDiagram, contributors);
        });
    }



    @Override
    @Transactional
    public void starPublicDiagram(int userId, UUID diagramId) {
        Diagram original = userDiagramService.getDiagramOrThrow(diagramId);
        PublicDiagram publicDiagram = getPublicDiagramOrThrow(original);

        PublicDiagramUserId starId = PublicDiagramUserId.builder()
                .userId(userId)
                .publicDiagramId(publicDiagram.getId())
                .build();

        if (starRepository.existsById(starId)) {
            return;
        }

        DiagramStar star = DiagramStar.builder()
                .id(starId)
                .publicDiagram(publicDiagram)
                .build();

        starRepository.save(star);

        publicDiagramRepository.incrementStar(publicDiagram.getId());
    }

    @Override
    @Transactional
    public void unstarPublicDiagram(int userId, UUID diagramId) {
        Diagram diagram = userDiagramService.getDiagramOrThrow(diagramId);
        PublicDiagram publicDiagram = getPublicDiagramOrThrow(diagram);

        PublicDiagramUserId id = PublicDiagramUserId.builder()
                .userId(userId)
                .publicDiagramId(publicDiagram.getId())
                .build();

        if (!starRepository.existsById(id)) {
            return;
        }

        starRepository.deleteById(id);

        publicDiagramRepository.decrementStar(publicDiagram.getId());
    }

    @Override
    @Transactional
    public Page<PublicDiagramInfoDto> getStaredPublicDiagrams(int userId, Pageable pageable) {

        Page<PublicDiagram> starred = starRepository.findStarredPublicDiagramsByUser(userId, pageable);

        return starred.map(publicDiagram -> {
            List<ContributorDto> contributors = userDiagramService.getContributors(publicDiagram.getId());
            return PublicDiagramInfoDto.toDto(publicDiagram, contributors);
        });
    }

    @Override
    @Transactional
//    @Cacheable(
//            value = "searchDiagramCache",
//            key = "#dto.searchPrompt + '-' + #dto.hashtags + '-' + #pageable.pageNumber + '-' + #pageable.pageSize"
//    )
    public Page<PublicDiagramInfoDto> searchPublicDiagrams(SearchRequestDto dto, Pageable pageable) {

        Page<PublicDiagram> page =
                publicDiagramRepository.searchPublicDiagrams(dto.getSearchPrompt(), dto.getHashtags(), pageable);

        return page.map(pd -> {
            List<ContributorDto> contributors = userDiagramService.getContributors(pd.getId());
            return PublicDiagramInfoDto.toDto(pd, contributors);
        });
    }

    @Override
    @Transactional
    public Page<PublicUserInfoDto> searchUsersByPublicDiagrams(SearchRequestDto dto,
                                                               Pageable pageable) {

        String search = dto.getSearchPrompt();
        if (search != null && search.isBlank()) {
            search = null;
        }

        Page<Object[]> raw = userRepository.searchUsersWithPublicStats(
                search,
                dto.getHashtags(),
                pageable
        );

        return raw.map(row -> {
            User user = (User) row[0];

            Long publicCount = row[1] == null ? 0L : ((Number) row[1]).longValue();
            Long totalStars  = row[2] == null ? 0L : ((Number) row[2]).longValue();
            Long score       = row[3] == null ? 0L : ((Number) row[3]).longValue();

            return PublicUserInfoDto.toDto(user, publicCount, totalStars, score);
        });
    }
}
