package backend.SQLOptimization;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import backend.SQLOptimization.dto.OptimizeSQLResponse;

/**
 * Unit tests for OptimizeSQLResponse DTO
 * Tests Lombok-generated methods for complete coverage
 */
class OptimizeSQLResponseTest {

    @Test
    void testNoArgsConstructor() {
        // When
        OptimizeSQLResponse response = new OptimizeSQLResponse();

        // Then
        assertNotNull(response);
        assertNull(response.getOptimizedSQL());
        assertNull(response.getSummary());
    }

    @Test
    void testAllArgsConstructor() {
        // Given
        String optimizedSQL = "SELECT id, name FROM users";
        String summary = "Optimized query";

        // When
        OptimizeSQLResponse response = new OptimizeSQLResponse(optimizedSQL, summary);

        // Then
        assertNotNull(response);
        assertEquals(optimizedSQL, response.getOptimizedSQL());
        assertEquals(summary, response.getSummary());
    }

    @Test
    void testGettersAndSetters() {
        // Given
        OptimizeSQLResponse response = new OptimizeSQLResponse();
        String optimizedSQL = "SELECT * FROM users WHERE id = 1";
        String summary = "Added WHERE clause";

        // When
        response.setOptimizedSQL(optimizedSQL);
        response.setSummary(summary);

        // Then
        assertEquals(optimizedSQL, response.getOptimizedSQL());
        assertEquals(summary, response.getSummary());
    }

    @Test
    void testSettersWithNull() {
        // Given
        OptimizeSQLResponse response = new OptimizeSQLResponse("test", "test");

        // When
        response.setOptimizedSQL(null);
        response.setSummary(null);

        // Then
        assertNull(response.getOptimizedSQL());
        assertNull(response.getSummary());
    }

    @Test
    void testEquals_SameObject() {
        // Given
        OptimizeSQLResponse response = new OptimizeSQLResponse("SELECT * FROM users", "Original");

        // When & Then
        assertEquals(response, response);
    }

    @Test
    void testEquals_EqualObjects() {
        // Given
        OptimizeSQLResponse response1 = new OptimizeSQLResponse(
            "SELECT id FROM users",
            "Optimized"
        );
        OptimizeSQLResponse response2 = new OptimizeSQLResponse(
            "SELECT id FROM users",
            "Optimized"
        );

        // When & Then
        assertEquals(response1, response2);
        assertEquals(response2, response1);
    }

    @Test
    void testEquals_DifferentOptimizedSQL() {
        // Given
        OptimizeSQLResponse response1 = new OptimizeSQLResponse("SELECT * FROM users", "Summary");
        OptimizeSQLResponse response2 = new OptimizeSQLResponse("SELECT id FROM users", "Summary");

        // When & Then
        assertNotEquals(response1, response2);
    }

    @Test
    void testEquals_DifferentSummary() {
        // Given
        OptimizeSQLResponse response1 = new OptimizeSQLResponse("SELECT * FROM users", "Summary 1");
        OptimizeSQLResponse response2 = new OptimizeSQLResponse("SELECT * FROM users", "Summary 2");

        // When & Then
        assertNotEquals(response1, response2);
    }

    @Test
    void testEquals_WithNull() {
        // Given
        OptimizeSQLResponse response = new OptimizeSQLResponse("SELECT * FROM users", "Summary");

        // When & Then
        assertNotEquals(response, null);
    }

    @Test
    void testEquals_DifferentClass() {
        // Given
        OptimizeSQLResponse response = new OptimizeSQLResponse("SELECT * FROM users", "Summary");
        String differentObject = "Not a response";

        // When & Then
        assertNotEquals(response, differentObject);
    }

    @Test
    void testHashCode_EqualObjects() {
        // Given
        OptimizeSQLResponse response1 = new OptimizeSQLResponse("SELECT * FROM users", "Summary");
        OptimizeSQLResponse response2 = new OptimizeSQLResponse("SELECT * FROM users", "Summary");

        // When & Then
        assertEquals(response1.hashCode(), response2.hashCode());
    }

    @Test
    void testHashCode_DifferentObjects() {
        // Given
        OptimizeSQLResponse response1 = new OptimizeSQLResponse("SELECT * FROM users", "Summary 1");
        OptimizeSQLResponse response2 = new OptimizeSQLResponse("SELECT id FROM users", "Summary 2");

        // When & Then
        assertNotEquals(response1.hashCode(), response2.hashCode());
    }

    @Test
    void testHashCode_Consistency() {
        // Given
        OptimizeSQLResponse response = new OptimizeSQLResponse("SELECT * FROM users", "Summary");

        // When
        int hashCode1 = response.hashCode();
        int hashCode2 = response.hashCode();

        // Then
        assertEquals(hashCode1, hashCode2);
    }

    @Test
    void testToString_ContainsFields() {
        // Given
        String optimizedSQL = "SELECT id, name FROM users";
        String summary = "Optimized to select specific columns";
        OptimizeSQLResponse response = new OptimizeSQLResponse(optimizedSQL, summary);

        // When
        String toString = response.toString();

        // Then
        assertNotNull(toString);
        assertTrue(toString.contains("optimizedSQL"));
        assertTrue(toString.contains("summary"));
        assertTrue(toString.contains(optimizedSQL));
        assertTrue(toString.contains(summary));
    }

    @Test
    void testToString_WithNullFields() {
        // Given
        OptimizeSQLResponse response = new OptimizeSQLResponse();

        // When
        String toString = response.toString();

        // Then
        assertNotNull(toString);
        assertTrue(toString.contains("optimizedSQL"));
        assertTrue(toString.contains("summary"));
    }

    @Test
    void testToString_NotNull() {
        // Given
        OptimizeSQLResponse response = new OptimizeSQLResponse("test", "test");

        // When
        String toString = response.toString();

        // Then
        assertNotNull(toString);
        assertFalse(toString.isEmpty());
    }

    @Test
    void testEquals_WithNullFields() {
        // Given
        OptimizeSQLResponse response1 = new OptimizeSQLResponse(null, null);
        OptimizeSQLResponse response2 = new OptimizeSQLResponse(null, null);

        // When & Then
        assertEquals(response1, response2);
        assertEquals(response1.hashCode(), response2.hashCode());
    }

    @Test
    void testEquals_OneNullField() {
        // Given
        OptimizeSQLResponse response1 = new OptimizeSQLResponse("SELECT * FROM users", null);
        OptimizeSQLResponse response2 = new OptimizeSQLResponse("SELECT * FROM users", "Summary");

        // When & Then
        assertNotEquals(response1, response2);
    }
}
