package backend.publicDiagramManagement.service;

import backend.entities.Diagram;
import backend.entities.joins.UserDiagram;
import backend.entities.publicDiagramEntities.CannedQueriesDiagrams;
import backend.entities.publicDiagramEntities.Hashtag;
import backend.entities.publicDiagramEntities.PublicDiagram;
import backend.publicDiagramManagement.dto.PublicDiagramDto;
import backend.publicDiagramManagement.dto.PublicDiagramInfoDto;
import backend.publicDiagramManagement.dto.get.ToBePublishedDiagramDto;
import backend.publicDiagramManagement.dto.publish.PublishDiagramRequestDto;
import backend.publicDiagramManagement.dto.search.SearchRequestDto;
import backend.publicDiagramManagement.dto.user.PublicUserInfoDto;
import backend.publicDiagramManagement.exceptions.PublicDiagramException;
import backend.publicDiagramManagement.repository.PublicDiagramRepository;
import backend.userDiagramManagement.dto.ContributorDto;
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

    public PublicDiagram getPublicDiagramOrThrow(UUID diagramId) {
        return publicDiagramRepository.findById(diagramId)
                .orElseThrow(() -> new PublicDiagramException.DiagramNotFoundException(
                        "Public diagram not found " + diagramId));
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


    @Override
    @Transactional
    public PublicDiagramDto viewPublicDiagram(int userId, UUID diagramId) {
        Diagram diagram = userDiagramService.getDiagramOrThrow(diagramId);
        PublicDiagram publicDiagram = getPublicDiagramOrThrow(diagramId);
        viewsService.addView(userId, publicDiagram);
        List<ContributorDto> contributors = userDiagramService.getContributors(diagramId);
        return PublicDiagramDto.toDto(diagram, publicDiagram, contributors);
    }

    @Override
    public PublicDiagramDto forkPublicDiagram(int userId, UUID diagramId) {
        return null;
    }

    @Override
    public Page<ToBePublishedDiagramDto> getToBePublishedDiagrams(int userId, Pageable pageable) {
        return null;
    }

    @Override
    public void starPublicDiagram(int userId, UUID diagramId) {

    }

    @Override
    public Page<DiagramInfoDto> getForkedPublicDiagrams(int userId, Pageable pageable) {
        return null;
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
