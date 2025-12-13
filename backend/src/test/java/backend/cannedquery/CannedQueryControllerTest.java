package backend.cannedquery;

import backend.security.AuthUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CannedQueryControllerTest {

    @InjectMocks
    private CannedQueryController controller;

    @Mock
    private CannedQueryService cannedQueryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllQueriesByDatabase_success_returnsOk() {
        List<CannedQueryDto> queries = List.of(new CannedQueryDto(), new CannedQueryDto());
        when(cannedQueryService.getAllQueriesByDatabase(1)).thenReturn(queries);

        AuthUser authUser = mock(AuthUser.class);
        ResponseEntity<?> response = controller.getAllQueriesByDatabase(1, authUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(queries, response.getBody());
    }

    @Test
    void getAllQueriesByDatabase_exception_returnsInternalServerError() {
        when(cannedQueryService.getAllQueriesByDatabase(1)).thenThrow(new RuntimeException("DB error"));

        AuthUser authUser = mock(AuthUser.class);
        ResponseEntity<?> response = controller.getAllQueriesByDatabase(1, authUser);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("DB error"));
    }

    @Test
    void getQueryById_success_returnsOk() {
        CannedQueryDto dto = new CannedQueryDto();
        when(cannedQueryService.getQueryById(1, 1)).thenReturn(dto);

        AuthUser authUser = mock(AuthUser.class);
        ResponseEntity<?> response = controller.getQueryById(1, 1, authUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void getQueryById_notFound_returnsNotFound() {
        when(cannedQueryService.getQueryById(1, 1)).thenThrow(new RuntimeException("Not found"));

        AuthUser authUser = mock(AuthUser.class);
        ResponseEntity<?> response = controller.getQueryById(1, 1, authUser);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Not found", response.getBody());
    }

    @Test
    void createQuery_success_returnsCreated() {
        CannedQueryDto dto = new CannedQueryDto();
        CannedQueryDto created = new CannedQueryDto();
        when(cannedQueryService.createQuery(dto)).thenReturn(created);

        AuthUser authUser = mock(AuthUser.class);
        ResponseEntity<?> response = controller.createQuery(dto, authUser);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(created, response.getBody());
    }

    @Test
    void createQuery_badRequest_returnsBadRequest() {
        CannedQueryDto dto = new CannedQueryDto();
        when(cannedQueryService.createQuery(dto)).thenThrow(new IllegalArgumentException("Invalid input"));

        AuthUser authUser = mock(AuthUser.class);
        ResponseEntity<?> response = controller.createQuery(dto, authUser);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid input", response.getBody());
    }

    @Test
    void createQuery_conflict_returnsConflict() {
        CannedQueryDto dto = new CannedQueryDto();
        when(cannedQueryService.createQuery(dto)).thenThrow(new RuntimeException("Already exists"));

        AuthUser authUser = mock(AuthUser.class);
        ResponseEntity<?> response = controller.createQuery(dto, authUser);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Already exists", response.getBody());
    }

    @Test
    void updateQuery_success_returnsOk() {
        CannedQueryDto dto = new CannedQueryDto();
        CannedQueryDto updated = new CannedQueryDto();
        when(cannedQueryService.updateQuery(1, dto)).thenReturn(updated);

        AuthUser authUser = mock(AuthUser.class);
        ResponseEntity<?> response = controller.updateQuery(1, dto, authUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(updated, response.getBody());
    }

    @Test
    void updateQuery_badRequest_returnsBadRequest() {
        CannedQueryDto dto = new CannedQueryDto();
        when(cannedQueryService.updateQuery(1, dto)).thenThrow(new IllegalArgumentException("Invalid name"));

        AuthUser authUser = mock(AuthUser.class);
        ResponseEntity<?> response = controller.updateQuery(1, dto, authUser);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid name", response.getBody());
    }

    @Test
    void updateQuery_notFound_returnsNotFound() {
        CannedQueryDto dto = new CannedQueryDto();
        when(cannedQueryService.updateQuery(1, dto)).thenThrow(new RuntimeException("Not found"));

        AuthUser authUser = mock(AuthUser.class);
        ResponseEntity<?> response = controller.updateQuery(1, dto, authUser);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Not found", response.getBody());
    }

    @Test
    void deleteQuery_success_returnsOk() {
        AuthUser authUser = mock(AuthUser.class);
        ResponseEntity<?> response = controller.deleteQuery(1, 1, authUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Query deleted successfully", response.getBody());
        verify(cannedQueryService).deleteQuery(1, 1);
    }

    @Test
    void deleteQuery_notFound_returnsNotFound() {
        doThrow(new RuntimeException("Not found")).when(cannedQueryService).deleteQuery(1, 1);

        AuthUser authUser = mock(AuthUser.class);
        ResponseEntity<?> response = controller.deleteQuery(1, 1, authUser);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Not found", response.getBody());
    }

    @Test
    void getAllQueriesByDatabase_internalServerError() {
        when(cannedQueryService.getAllQueriesByDatabase(1))
                .thenThrow(new RuntimeException("Unexpected error"));

        AuthUser authUser = mock(AuthUser.class);
        ResponseEntity<?> response = controller.getAllQueriesByDatabase(1, authUser);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Unexpected error"));
    }

    @Test
    void getQueryById_internalServerError() {
        when(cannedQueryService.getQueryById(1, 1))
                .thenThrow(new RuntimeException("Unexpected error"));

        AuthUser authUser = mock(AuthUser.class);
        ResponseEntity<?> response = controller.getQueryById(1, 1, authUser);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Unexpected error"));
    }

    @Test
    void createQuery_internalServerError() {
        CannedQueryDto dto = new CannedQueryDto();
        when(cannedQueryService.createQuery(dto))
                .thenThrow(new RuntimeException("Unexpected error"));

        AuthUser authUser = mock(AuthUser.class);
        ResponseEntity<?> response = controller.createQuery(dto, authUser);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Unexpected error"));
    }

    @Test
    void updateQuery_internalServerError() {
        CannedQueryDto dto = new CannedQueryDto();
        when(cannedQueryService.updateQuery(1, dto))
                .thenThrow(new RuntimeException("Unexpected error"));

        AuthUser authUser = mock(AuthUser.class);
        ResponseEntity<?> response = controller.updateQuery(1, dto, authUser);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Unexpected error"));
    }

    @Test
    void deleteQuery_internalServerError() {
        doThrow(new RuntimeException("Unexpected error"))
                .when(cannedQueryService).deleteQuery(1, 1);

        AuthUser authUser = mock(AuthUser.class);
        ResponseEntity<?> response = controller.deleteQuery(1, 1, authUser);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Unexpected error"));
    }
}
