package backend.publicDiagramManagement.service;

import backend.entities.Diagram;
import backend.entities.joins.UserDiagram;
import backend.entities.publicDiagramEntities.CannedQueriesDiagrams;
import backend.entities.publicDiagramEntities.Hashtag;
import backend.entities.publicDiagramEntities.PublicDiagram;
import backend.publicDiagramManagement.dto.PublicDiagramDraftDto;
import backend.publicDiagramManagement.dto.PublicDiagramDto;
import backend.publicDiagramManagement.dto.publish.PublishDiagramDto;
import backend.publicDiagramManagement.dto.publish.PublishDiagramRequestDto;
import backend.publicDiagramManagement.exceptions.PublicDiagramException;
import backend.publicDiagramManagement.repository.PublicDiagramRepository;
import backend.userDiagramManagement.dto.ContributorDto;
import backend.userDiagramManagement.repository.DiagramRepository;
import backend.userDiagramManagement.repository.UserDiagramRepository;
import backend.userDiagramManagement.service.UserDiagramService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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



        UserDiagram userDiagram = userDiagramService.getUserDiagramOrThrow(userId, dto.getId());
        userDiagramService.checkOwner(userDiagram, "publish");

        PublicDiagram publicDiagram = publicDiagramRepository.findById(dto.getId())
                .orElse(new PublicDiagram());

        publicDiagram.setDiagram(userDiagram.getDiagram());
        publicDiagram.setShortDescription(dto.getShortDescription());
        publicDiagram.setDetailedDescription(dto.getDetailedDescription());
        publicDiagram.setDdl(dto.getDdl());

        // Handle hashtags
        Set<Hashtag> hashtags = hashtagService.resolveHashtags(new HashSet<>(dto.getHashTags()));
        publicDiagram.setHashtags(hashtags);

        // Handle canned queries
        Set<CannedQueriesDiagrams> queries = dto.getCannedQueries().stream()
                .map(q -> {
                    CannedQueriesDiagrams entity = new CannedQueriesDiagrams();
                    entity.setName(q.getName());
                    entity.setDescription(q.getDescription());
                    entity.setQuery(q.getQuery());
                    entity.setDiagram(publicDiagram);
                    return entity;
                })
                .collect(Collectors.toSet());

        publicDiagram.getQueries().clear();
        publicDiagram.getQueries().addAll(queries);

        publicDiagram.setStatus(PublicDiagramStatus.PUBLISHED);

        if (publicDiagram.getPublishedAt() == null) {
            publicDiagram.setPublishedAt(new Date(System.currentTimeMillis()));
        }

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
    public PublicDiagramDraftDto getDraftPublicDiagram(int userId, UUID diagramId) {
        UserDiagram userDiagram = userDiagramService.getUserDiagramOrThrow(userId, diagramId);
        userDiagramService.checkOwner(userDiagram, "publish");
        Diagram diagram = userDiagram.getDiagram();
        return
    }


}
