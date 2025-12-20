package backend.collaboration;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import backend.entities.Diagram;
import backend.userDiagramManagement.repository.DiagramRepository;
import jakarta.persistence.EntityNotFoundException;

@Service
public class DiagramService {

    @Autowired
    private DiagramRepository diagramRepository;

    @Transactional
    public void saveSnapshot(UUID diagramId, byte[] snapshotData) {
        Diagram diagram = diagramRepository.findById(diagramId)
                .orElseThrow(() -> new EntityNotFoundException("Diagram not found with ID: " + diagramId));

        // Update the content
        diagram.setContent(snapshotData);
        
        // Note: @UpdateTimestamp in your entity will automatically update 'lastModified'
        // when we save, so we don't need to manually set the date.
        diagramRepository.save(diagram);
    }

    @Transactional(readOnly = true)
    public byte[] getSnapshot(UUID diagramId) {
        Diagram diagram = diagramRepository.findById(diagramId)
                .orElseThrow(() -> new EntityNotFoundException("Diagram not found with ID: " + diagramId));

        return diagram.getContent();
    }
}