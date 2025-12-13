package backend.cannedquery;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import backend.databaseManagement.UserDatabaseRepository;
import backend.entities.CannedQueriesDB;
import backend.entities.UserDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class CannedQueryServiceTest {

    @Mock
    private CannedQueryRepository cannedQueryRepository;

    @Mock
    private UserDatabaseRepository userDatabaseRepository;

    @InjectMocks
    private CannedQueryService cannedQueryService;

    private CannedQueriesDB query1;
    private CannedQueriesDB query2;
    private UserDatabase database;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        database = new UserDatabase();
        database.setId(1);
        database.setName("TestDB");

        query1 = new CannedQueriesDB();
        query1.setId(1);
        query1.setName("Query1");
        query1.setQuery("SELECT * FROM table1");
        query1.setDatabase(database);
        query1.setCreatedAt(LocalDateTime.now());
        query1.setUpdatedAt(LocalDateTime.now());

        query2 = new CannedQueriesDB();
        query2.setId(2);
        query2.setName("Query2");
        query2.setQuery("SELECT * FROM table2");
        query2.setDatabase(database);
        query2.setCreatedAt(LocalDateTime.now());
        query2.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void testGetAllQueriesByDatabase() {
        when(cannedQueryRepository.findByDatabaseId(1)).thenReturn(Arrays.asList(query1, query2));

        List<CannedQueryDto> result = cannedQueryService.getAllQueriesByDatabase(1);
        assertEquals(2, result.size());
        assertEquals("Query1", result.get(0).getName());
        assertEquals("Query2", result.get(1).getName());
    }

    @Test
    void testGetQueryById_Found() {
        when(cannedQueryRepository.findByIdAndDatabaseId(1, 1)).thenReturn(query1);

        CannedQueryDto dto = cannedQueryService.getQueryById(1, 1);
        assertEquals("Query1", dto.getName());
        assertEquals("SELECT * FROM table1", dto.getQuery());
    }

    @Test
    void testGetQueryById_NotFound() {
        when(cannedQueryRepository.findByIdAndDatabaseId(99, 1)).thenReturn(null);
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            cannedQueryService.getQueryById(99, 1)
        );
        assertEquals("Canned query not found", exception.getMessage());
    }

    @Test
    void testCreateQuery_Success() {
        CannedQueryDto dto = new CannedQueryDto();
        dto.setName("Query3");
        dto.setQuery("SELECT * FROM table3");
        dto.setDatabaseId(1);

        when(userDatabaseRepository.findById(1)).thenReturn(Optional.of(database));
        when(cannedQueryRepository.existsByNameAndDatabaseId("Query3", 1)).thenReturn(false);

        CannedQueriesDB savedQuery = new CannedQueriesDB();
        savedQuery.setId(3);
        savedQuery.setName("Query3");
        savedQuery.setQuery("SELECT * FROM table3");
        savedQuery.setDatabase(database);
        savedQuery.setCreatedAt(LocalDateTime.now());
        savedQuery.setUpdatedAt(LocalDateTime.now());

        when(cannedQueryRepository.save(any(CannedQueriesDB.class))).thenReturn(savedQuery);

        CannedQueryDto result = cannedQueryService.createQuery(dto);

        assertEquals("Query3", result.getName());
        assertEquals("SELECT * FROM table3", result.getQuery());
    }

    @Test
    void testUpdateQuery_Success() {
        CannedQueryDto dto = new CannedQueryDto();
        dto.setName("UpdatedQuery");
        dto.setQuery("SELECT * FROM table_updated");
        dto.setDatabaseId(1);

        when(cannedQueryRepository.findByIdAndDatabaseId(1, 1)).thenReturn(query1);
        when(cannedQueryRepository.existsByNameAndDatabaseId("UpdatedQuery", 1)).thenReturn(false);

        query1.setName("UpdatedQuery");
        query1.setQuery("SELECT * FROM table_updated");
        when(cannedQueryRepository.save(query1)).thenReturn(query1);

        CannedQueryDto result = cannedQueryService.updateQuery(1, dto);
        assertEquals("UpdatedQuery", result.getName());
        assertEquals("SELECT * FROM table_updated", result.getQuery());
    }

    @Test
    void testDeleteQuery_Success() {
        when(cannedQueryRepository.findByIdAndDatabaseId(1, 1)).thenReturn(query1);

        assertDoesNotThrow(() -> cannedQueryService.deleteQuery(1, 1));
        verify(cannedQueryRepository, times(1)).delete(query1);
    }

    @Test
    void testDeleteQuery_NotFound() {
        when(cannedQueryRepository.findByIdAndDatabaseId(99, 1)).thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            cannedQueryService.deleteQuery(99, 1)
        );
        assertEquals("Canned query not found", exception.getMessage());
    }

    @Test
    void testCreateQuery_MissingName() {
        CannedQueryDto dto = new CannedQueryDto();
        dto.setQuery("SELECT * FROM table");
        dto.setDatabaseId(1);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            cannedQueryService.createQuery(dto)
        );
        assertEquals("Query name is required", exception.getMessage());
    }

    @Test
    void testCreateQuery_MissingQueryBody() {
        CannedQueryDto dto = new CannedQueryDto();
        dto.setName("MyQuery");
        dto.setDatabaseId(1);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            cannedQueryService.createQuery(dto)
        );
        assertEquals("Query body is required", exception.getMessage());
    }

    @Test
    void testCreateQuery_MissingDatabaseId() {
        CannedQueryDto dto = new CannedQueryDto();
        dto.setName("MyQuery");
        dto.setQuery("SELECT * FROM table");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            cannedQueryService.createQuery(dto)
        );
        assertEquals("Database ID is required", exception.getMessage());
    }

    @Test
    void testCreateQuery_DatabaseNotFound() {
        CannedQueryDto dto = new CannedQueryDto();
        dto.setName("MyQuery");
        dto.setQuery("SELECT * FROM table");
        dto.setDatabaseId(99);

        when(userDatabaseRepository.findById(99)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            cannedQueryService.createQuery(dto)
        );
        assertEquals("Database not found", exception.getMessage());
    }

    @Test
    void testCreateQuery_DuplicateName() {
        CannedQueryDto dto = new CannedQueryDto();
        dto.setName("Query1");
        dto.setQuery("SELECT * FROM table");
        dto.setDatabaseId(1);

        when(userDatabaseRepository.findById(1)).thenReturn(Optional.of(database));
        when(cannedQueryRepository.existsByNameAndDatabaseId("Query1", 1)).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            cannedQueryService.createQuery(dto)
        );
        assertEquals("A query with this name already exists for this database", exception.getMessage());
    }

    @Test
    void testUpdateQuery_NotFound() {
        CannedQueryDto dto = new CannedQueryDto();
        dto.setName("UpdatedQuery");
        dto.setQuery("SELECT * FROM table");
        dto.setDatabaseId(1);

        when(cannedQueryRepository.findByIdAndDatabaseId(99, 1)).thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            cannedQueryService.updateQuery(99, dto)
        );
        assertEquals("Canned query not found", exception.getMessage());
    }

    @Test
    void testUpdateQuery_DuplicateName() {
        CannedQueryDto dto = new CannedQueryDto();
        dto.setName("Query2"); // already exists
        dto.setQuery("SELECT * FROM updated_table");
        dto.setDatabaseId(1);

        when(cannedQueryRepository.findByIdAndDatabaseId(1, 1)).thenReturn(query1);
        when(cannedQueryRepository.existsByNameAndDatabaseId("Query2", 1)).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            cannedQueryService.updateQuery(1, dto)
        );
        assertEquals("A query with this name already exists for this database", exception.getMessage());
    }

    @Test
    void testUpdateQuery_MissingName() {
        CannedQueryDto dto = new CannedQueryDto();
        dto.setName("");
        dto.setQuery("SELECT * FROM table");
        dto.setDatabaseId(1);

        when(cannedQueryRepository.findByIdAndDatabaseId(1, 1)).thenReturn(query1);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            cannedQueryService.updateQuery(1, dto)
        );
        assertEquals("Query name is required", exception.getMessage());
    }

    @Test
    void testUpdateQuery_MissingQueryBody() {
        CannedQueryDto dto = new CannedQueryDto();
        dto.setName("UpdatedQuery");
        dto.setQuery("");
        dto.setDatabaseId(1);

        when(cannedQueryRepository.findByIdAndDatabaseId(1, 1)).thenReturn(query1);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            cannedQueryService.updateQuery(1, dto)
        );
        assertEquals("Query body is required", exception.getMessage());
    }

}
