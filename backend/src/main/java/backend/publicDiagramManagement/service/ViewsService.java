package backend.publicDiagramManagement.service;

import backend.entities.joins.UserDiagramId;
import backend.entities.publicDiagramEntities.DiagramView;
import backend.entities.publicDiagramEntities.PublicDiagram;
import backend.publicDiagramManagement.repository.PublicDiagramRepository;
import backend.publicDiagramManagement.repository.ViewsRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class ViewsService {

    ViewsRepository viewsRepository;
    PublicDiagramRepository publicDiagramRepository;

    @Transactional
    public void addView(int userId, PublicDiagram publicDiagram) {
        int inserted = viewsRepository.insertIfNotExists(userId, publicDiagram.getId());

        if (inserted == 1)
            publicDiagramRepository.incrementViews(publicDiagram.getId());
    }
}
