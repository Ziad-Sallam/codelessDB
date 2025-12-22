package backend.publicDiagramManagement.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import backend.publicDiagramManagement.repository.PublicDiagramRepository;
import backend.publicDiagramManagement.repository.ViewsRepository;

@ExtendWith(MockitoExtension.class)
class ViewsServiceTest {

    @Mock
    private ViewsRepository viewsRepository;

    @Mock
    private PublicDiagramRepository publicDiagramRepository;

    @InjectMocks
    private ViewsService viewsService;

    @Test
    void testAddView_Success() {
        int userId = 1;
        UUID diagramId = UUID.randomUUID();

        when(viewsRepository.insertIfNotExists(userId, diagramId)).thenReturn(1);

        boolean result = viewsService.addView(userId, diagramId);

        assertTrue(result);
        verify(publicDiagramRepository, times(1)).incrementViews(diagramId);
    }

    @Test
    void testAddView_AlreadyCounted() {
        int userId = 1;
        UUID diagramId = UUID.randomUUID();

        when(viewsRepository.insertIfNotExists(userId, diagramId)).thenReturn(0);

        boolean result = viewsService.addView(userId, diagramId);

        assertFalse(result);
        verify(publicDiagramRepository, never()).incrementViews(any());
    }
}
