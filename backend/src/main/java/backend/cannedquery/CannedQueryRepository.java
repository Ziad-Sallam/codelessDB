package backend.cannedquery;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import backend.entities.CannedQueriesDB;

@Repository
public interface CannedQueryRepository extends JpaRepository<CannedQueriesDB, Integer> {
    List<CannedQueriesDB> findByDatabaseId(Integer databaseId);

    CannedQueriesDB findByIdAndDatabaseId(Integer id, Integer databaseId);

    boolean existsByNameAndDatabaseId(String name, Integer databaseId);
}