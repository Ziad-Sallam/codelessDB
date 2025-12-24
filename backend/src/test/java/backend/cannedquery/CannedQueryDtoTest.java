

package backend.cannedquery;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import backend.entities.CannedQueriesDB;
import backend.entities.UserDatabase;
import backend.cannedquery.CannedQueryDto;

import org.junit.jupiter.api.Test;

class CannedQueryDtoTest {

    @Test
    void constructor_entity_mapsAllFieldsCorrectly() {
        // Arrange
        UserDatabase mockDatabase = mock(UserDatabase.class);
        when(mockDatabase.getId()).thenReturn(100);
        when(mockDatabase.getName()).thenReturn("TestDB");

        CannedQueriesDB entity = mock(CannedQueriesDB.class);
        when(entity.getId()).thenReturn(1);
        when(entity.getName()).thenReturn("Query1");
        when(entity.getDescription()).thenReturn("Test description");
        when(entity.getQuery()).thenReturn("SELECT * FROM users");
        when(entity.getCreatedAt()).thenReturn(LocalDateTime.of(2025, 12, 24, 10, 0));
        when(entity.getUpdatedAt()).thenReturn(LocalDateTime.of(2025, 12, 24, 11, 0));
        when(entity.getDatabase()).thenReturn(mockDatabase);

        // Act
        CannedQueryDto dto = new CannedQueryDto(entity);

        // Assert
        assertEquals(1, dto.getId());
        assertEquals("Query1", dto.getName());
        assertEquals("Test description", dto.getDescription());
        assertEquals("SELECT * FROM users", dto.getQuery());
        assertEquals(LocalDateTime.of(2025, 12, 24, 10, 0), dto.getCreatedAt());
        assertEquals(LocalDateTime.of(2025, 12, 24, 11, 0), dto.getUpdatedAt());
        assertEquals(100, dto.getDatabaseId());
        assertEquals("TestDB", dto.getDatabaseName());
    }
}
