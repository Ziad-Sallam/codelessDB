package backend.publicDiagramManagement.service;

import backend.entities.joins.UserDiagramId;
import backend.entities.publicDiagramEntities.DiagramView;
import backend.entities.publicDiagramEntities.PublicDiagram;
import backend.publicDiagramManagement.repository.PublicDiagramRepository;
import backend.publicDiagramManagement.repository.ViewsRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
@RequiredArgsConstructor
public class ViewsService {

    ViewsRepository viewsRepository;
    PublicDiagramRepository publicDiagramRepository;

    @Transactional
    public boolean addView(int userId, UUID diagramId) {

        int inserted = viewsRepository.insertIfNotExists(userId, diagramId);

        if (inserted == 1) {
            publicDiagramRepository.incrementViews(diagramId);
            return true; // view was counted
        }

        return false; // user already viewed
    }
}
