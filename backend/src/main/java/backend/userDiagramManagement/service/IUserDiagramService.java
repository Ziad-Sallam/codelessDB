package backend.userDiagramManagement.service;

import backend.userDiagramManagement.dto.DiagramDto;
import backend.userDiagramManagement.dto.DiagramInfoDto;
import backend.userDiagramManagement.dto.create.DiagramCreateRequestDto;
import backend.userDiagramManagement.dto.get.DiagramGetInfoRequestDto;
import backend.userDiagramManagement.dto.search.DiagramSearchRequestDto;
import backend.userDiagramManagement.dto.share.DiagramShareRequestDto;
import backend.userDiagramManagement.dto.share.DiagramShareResponseDto;
import backend.userDiagramManagement.dto.update.DiagramUpdateRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Date;
import java.util.UUID;

public interface IUserDiagramService {
    UUID createDiagram(int userId, DiagramCreateRequestDto request);
    Date updateDiagram(int userId, DiagramUpdateRequestDto request, UUID diagramID);
    void deleteDiagram(int userId, UUID id);
    DiagramDto searchDiagramById(int userId, UUID id);
    Page<DiagramDto> searchDiagrams(int userId, DiagramSearchRequestDto request, Pageable pageable);
    DiagramShareResponseDto shareDiagram(int userId, DiagramShareRequestDto request);
    Page<DiagramInfoDto> getDiagramsByUserId(int userId, DiagramGetInfoRequestDto request, Pageable pageable);
}
