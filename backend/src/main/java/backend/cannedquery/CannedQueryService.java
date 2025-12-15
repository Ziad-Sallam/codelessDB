package backend.cannedquery;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import backend.databaseManagement.UserDatabaseRepository;
import backend.entities.CannedQueriesDB;
import backend.entities.UserDatabase;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CannedQueryService {

    private final CannedQueryRepository cannedQueryRepository;

    private final UserDatabaseRepository userDatabaseRepository;

    public List<CannedQueryDto> getAllQueriesByDatabase(Integer databaseId) {
        List<CannedQueriesDB> queries = cannedQueryRepository.findByDatabaseId(databaseId);
        return queries.stream()
                .map(CannedQueryDto::new)
                .collect(Collectors.toList());
    }

    public CannedQueryDto getQueryById(Integer id, Integer databaseId) {
        CannedQueriesDB query = cannedQueryRepository.findByIdAndDatabaseId(id, databaseId);
        if (query == null) {
            throw new RuntimeException("Canned query not found");
        }
        return new CannedQueryDto(query);
    }

    @Transactional
    public CannedQueryDto createQuery(CannedQueryDto dto) {
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Query name is required");
        }
        if (dto.getQuery() == null || dto.getQuery().trim().isEmpty()) {
            throw new IllegalArgumentException("Query body is required");
        }
        if (dto.getDatabaseId() == null) {
            throw new IllegalArgumentException("Database ID is required");
        }

        UserDatabase database = userDatabaseRepository.findById(dto.getDatabaseId())
                .orElseThrow(() -> new RuntimeException("Database not found"));

        if (cannedQueryRepository.existsByNameAndDatabaseId(dto.getName(), dto.getDatabaseId())) {
            throw new RuntimeException("A query with this name already exists for this database");
        }

        CannedQueriesDB entity = new CannedQueriesDB();
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setQuery(dto.getQuery());
        entity.setDatabase(database);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());

        CannedQueriesDB saved = cannedQueryRepository.save(entity);
        return new CannedQueryDto(saved);
    }

    @Transactional
    public CannedQueryDto updateQuery(Integer id, CannedQueryDto dto) {
        CannedQueriesDB existing = cannedQueryRepository.findByIdAndDatabaseId(id, dto.getDatabaseId());
        if (existing == null) {
            throw new RuntimeException("Canned query not found");
        }

        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Query name is required");
        }
        if (dto.getQuery() == null || dto.getQuery().trim().isEmpty()) {
            throw new IllegalArgumentException("Query body is required");
        }
        if (!existing.getName().equals(dto.getName())) {
            if (cannedQueryRepository.existsByNameAndDatabaseId(dto.getName(), dto.getDatabaseId())) {
                throw new RuntimeException("A query with this name already exists for this database");
            }
        }
        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        existing.setQuery(dto.getQuery());
        existing.setUpdatedAt(LocalDateTime.now());
        CannedQueriesDB updated = cannedQueryRepository.save(existing);
        return new CannedQueryDto(updated);
    }

    @Transactional
    public void deleteQuery(Integer id, Integer databaseId) {
        CannedQueriesDB existing = cannedQueryRepository.findByIdAndDatabaseId(id, databaseId);
        if (existing == null) {
            throw new RuntimeException("Canned query not found");
        }
        cannedQueryRepository.delete(existing);
    }
}