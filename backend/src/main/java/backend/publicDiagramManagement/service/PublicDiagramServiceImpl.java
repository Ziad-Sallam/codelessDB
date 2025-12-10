package backend.publicDiagramManagement.service;

import backend.entities.Diagram;
import backend.entities.User;
import backend.entities.joins.PublicDiagramUserId;
import backend.entities.joins.UserDiagram;
import backend.entities.publicDiagramEntities.CannedQueriesDiagrams;
import backend.entities.publicDiagramEntities.DiagramFork;
import backend.entities.publicDiagramEntities.Hashtag;
import backend.entities.publicDiagramEntities.PublicDiagram;
import backend.publicDiagramManagement.dto.PublicDiagramDto;
import backend.publicDiagramManagement.dto.PublicDiagramInfoDto;
import backend.publicDiagramManagement.dto.get.ToBePublishedDiagramDto;
import backend.publicDiagramManagement.dto.publish.PublishDiagramRequestDto;
import backend.publicDiagramManagement.dto.search.SearchRequestDto;
import backend.publicDiagramManagement.dto.user.PublicUserInfoDto;
import backend.publicDiagramManagement.exceptions.PublicDiagramException;
import backend.publicDiagramManagement.repository.ForkRepository;
import backend.publicDiagramManagement.repository.PublicDiagramRepository;
import backend.publicDiagramManagement.repository.ViewsRepository;
import backend.user.Role;
import backend.user.UserService;
import backend.userDiagramManagement.dto.ContributorDto;
import backend.userDiagramManagement.dto.DiagramDto;
import backend.userDiagramManagement.dto.DiagramInfoDto;
import backend.userDiagramManagement.repository.DiagramRepository;
import backend.userDiagramManagement.repository.UserDiagramRepository;
import backend.userDiagramManagement.service.UserDiagramService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.sql.Date;



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
    private final UserService userService;

    public PublicDiagram getPublicDiagramOrThrow(UUID diagramId) {
        return publicDiagramRepository.findById(diagramId)
                .orElseThrow(() -> new PublicDiagramException.DiagramNotFoundException(
                        "Public diagram not found " + diagramId));
    }

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
    public Page<DiagramInfoDto> getForkedPublicDiagrams(int userId, Pageable pageable) {

        Page<Diagram> forkedDiagrams =
                forkRepository.findForkedDiagramsByUser(userId, pageable);

        return forkedDiagrams.map(diagram -> {

            List<ContributorDto> contributors =
                    userDiagramService.getContributors(diagram.getId());

            Role role = Role.OWNER;

            return DiagramInfoDto.toDto(diagram, role, contributors);
        });
    }

    @Override
    public void starPublicDiagram(int userId, UUID diagramId) {
        
    }

    @Override
    public Page<DiagramInfoDto> getStaredPublicDiagrams(int userId, Pageable pageable) {
        return null;
    }

    @Override
    public Page<PublicDiagramInfoDto> searchPublicDiagrams(SearchRequestDto searchRequestDto, Pageable pageable) {
        return null;
    }

    @Override
    public Page<PublicUserInfoDto> searchUsersByPublicDiagrams(SearchRequestDto searchRequestDto, Pageable pageable) {
        return null;
    }
}
