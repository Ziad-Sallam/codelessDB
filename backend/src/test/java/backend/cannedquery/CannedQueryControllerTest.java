package backend.cannedquery;

import backend.security.AuthUser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import backend.cannedquery.exception.CannedQueryException.CannedQueryNotFoundException;
import backend.databaseManagement.exception.DatabaseException.MissingFieldException;
import backend.cannedquery.exception.CannedQueryException.CannedQueryAlreadyExistsException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import backend.databaseManagement.exception.DatabaseException;

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
        when(cannedQueryService.getAllQueriesByDatabase(1)).thenThrow(new CannedQueryNotFoundException("DB error"));

        AuthUser authUser = mock(AuthUser.class);
        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> controller.getAllQueriesByDatabase(1, authUser));

        assertTrue(ex.getMessage().contains("DB error"));
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
        when(cannedQueryService.getQueryById(1, 1)).thenThrow(new CannedQueryNotFoundException("Not found"));

        AuthUser authUser = mock(AuthUser.class);
        CannedQueryNotFoundException ex = assertThrows(
                CannedQueryNotFoundException.class,
                () -> controller.getQueryById(1, 1, authUser));
        assertTrue(ex.getMessage().contains("Not found"));
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
        when(cannedQueryService.createQuery(dto)).thenThrow(new MissingFieldException("Invalid input"));

        AuthUser authUser = mock(AuthUser.class);
        MissingFieldException ex = assertThrows(
                MissingFieldException.class,
                () -> controller.createQuery(dto, authUser));

        assertTrue(ex.getMessage().contains("Invalid input"));
    }

    @Test
    void createQuery_conflict_returnsConflict() {
        CannedQueryDto dto = new CannedQueryDto();
        when(cannedQueryService.createQuery(dto)).thenThrow(new CannedQueryAlreadyExistsException("Already exists"));

        AuthUser authUser = mock(AuthUser.class);

        CannedQueryAlreadyExistsException ex = assertThrows(
                CannedQueryAlreadyExistsException.class,
                () -> controller.createQuery(dto, authUser));

        assertTrue(ex.getMessage().contains("Already exists"));
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
        when(cannedQueryService.updateQuery(1, dto))
                .thenThrow(new DatabaseException.MissingFieldException("Invalid name"));

        AuthUser authUser = mock(AuthUser.class);

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> controller.updateQuery(1, dto, authUser));

        assertTrue(ex.getMessage().contains("Invalid name"));
    }

    @Test
    void updateQuery_notFound_returnsNotFound() {
        CannedQueryDto dto = new CannedQueryDto();
        when(cannedQueryService.updateQuery(1, dto)).thenThrow(new RuntimeException("Not found"));

        AuthUser authUser = mock(AuthUser.class);
        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> controller.updateQuery(1, dto, authUser));

        assertTrue(ex.getMessage().contains("Not found"));
    }

    @Test
    void deleteQuery_success_returnsNoContent() {
        AuthUser authUser = mock(AuthUser.class);

        ResponseEntity<Void> response = controller.deleteQuery(1, 1, authUser);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody()); // 204 responses have no body
        verify(cannedQueryService).deleteQuery(1, 1);
    }

    @Test
    void deleteQuery_notFound_returnsNotFound() {
        doThrow(new RuntimeException("Not found")).when(cannedQueryService).deleteQuery(1, 1);

        AuthUser authUser = mock(AuthUser.class);
        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> controller.deleteQuery(1, 1, authUser));

        assertTrue(ex.getMessage().contains("Not found"));
    }

    @Test
    void getAllQueriesByDatabase_internalServerError() {
        when(cannedQueryService.getAllQueriesByDatabase(1))
                .thenThrow(new RuntimeException("Unexpected error"));

        AuthUser authUser = mock(AuthUser.class);
        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> controller.getAllQueriesByDatabase(1, authUser));

        assertTrue(ex.getMessage().contains("Unexpected error"));
    }

    @Test
    void getQueryById_internalServerError() {
        when(cannedQueryService.getQueryById(1, 1))
                .thenThrow(new RuntimeException("Unexpected error"));

        AuthUser authUser = mock(AuthUser.class);

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> controller.getQueryById(1, 1, authUser));

        assertTrue(ex.getMessage().contains("Unexpected error"));
    }

    @Test
    void createQuery_internalServerError() {
        CannedQueryDto dto = new CannedQueryDto();
        when(cannedQueryService.createQuery(dto))
                .thenThrow(new RuntimeException("Unexpected error"));

        AuthUser authUser = mock(AuthUser.class);

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> controller.createQuery(dto, authUser));

        assertTrue(ex.getMessage().contains("Unexpected error"));
    }

    @Test
    void updateQuery_internalServerError() {
        CannedQueryDto dto = new CannedQueryDto();
        when(cannedQueryService.updateQuery(1, dto))
                .thenThrow(new RuntimeException("Unexpected error"));

        AuthUser authUser = mock(AuthUser.class);

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> controller.updateQuery(1, dto, authUser));

        assertTrue(ex.getMessage().contains("Unexpected error"));
    }

    @Test
    void deleteQuery_internalServerError() {
        doThrow(new RuntimeException("Unexpected error"))
                .when(cannedQueryService).deleteQuery(1, 1);

        AuthUser authUser = mock(AuthUser.class);

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> controller.deleteQuery(1, 1, authUser));

        assertTrue(ex.getMessage().contains("Unexpected error"));
    }

}
