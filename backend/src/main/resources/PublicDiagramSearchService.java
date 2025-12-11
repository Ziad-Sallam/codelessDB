package backend.publicDiagramManagement.service;

import backend.entities.publicDiagramEntities.PublicDiagram;
import backend.publicDiagramManagement.dto.PublicDiagramInfoDto;
import backend.publicDiagramManagement.dto.search.SearchRequestDto;
import backend.userDiagramManagement.service.UserDiagramService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PublicDiagramSearchService {

    private final EntityManager entityManager;
    private final UserDiagramService userDiagramService;

    @Transactional(readOnly = true)   // <--- use Spring's Transactional
    @Cacheable(
            value = "searchDiagramCache",
            key = "#dto.searchPrompt + '-' + #dto.hashtags + '-' + #pageable.pageNumber + '-' + #pageable.pageSize"
    )
    public Page<PublicDiagramInfoDto> searchPublicDiagrams(SearchRequestDto dto,
                                                           Pageable pageable) {

        SearchSession searchSession = Search.session(entityManager);

        var query = searchSession.search(PublicDiagram.class)
                .where(f -> {
                    var bool = f.bool();

                    // ----- text search -----
                    if (dto.getSearchPrompt() != null && !dto.getSearchPrompt().isBlank()) {
                        bool.must(
                                f.simpleQueryString()
                                        .fields("diagramName", "shortDescription", "detailedDescription")
                                        .matching(dto.getSearchPrompt())
                        );
                    }

                    // ----- hashtags -----
                    if (dto.getHashtags() != null && !dto.getHashtags().isEmpty()) {
                        bool.must(
                                f.terms()
                                        .field("hashtags.name")
                                        .matchingAny(dto.getHashtags())
                        );
                    }

                    return bool;
                })
                .toQuery(); // <-- build the SearchQuery here

        int offset = (int) pageable.getOffset();
        int limit = pageable.getPageSize();

        List<PublicDiagram> hits = query.fetchHits(offset, limit);
        long total = query.fetchTotal().hitCount();

        List<PublicDiagramInfoDto> mapped = hits.stream()
                .map(pd -> {
                    var contributors = userDiagramService.getContributors(pd.getId());
                    return PublicDiagramInfoDto.toDto(pd, contributors);
                })
                .toList();

        return new PageImpl<>(mapped, pageable, total);
    }
}
