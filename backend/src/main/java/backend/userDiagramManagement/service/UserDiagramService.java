package backend.userDiagramManagement.service;

import backend.userDiagramManagement.dto.create.DiagramCreateRequestDto;
import backend.userDiagramManagement.dto.search.DiagramSearchRequestDto;
import backend.userDiagramManagement.dto.share.DiagramShareResponseDto;
import backend.userDiagramManagement.dto.update.DiagramUpdateRequestDto;
import backend.userDiagramManagement.dto.share.DiagramShareRequestDto;
import backend.userDiagramManagement.dto.DiagramDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserDiagramService implements IUserDiagramService {

    @Override
    public UUID createDiagram(int userId, DiagramCreateRequestDto request) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public LocalDateTime updateDiagram(int userId, DiagramUpdateRequestDto request) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void deleteDiagram(int userId, UUID id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public DiagramDto searchDiagramById(int userId, UUID id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Page<DiagramDto> searchDiagrams(int userId, DiagramSearchRequestDto request, Pageable pageable) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public DiagramShareResponseDto shareDiagram(int userId, DiagramShareRequestDto request) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
