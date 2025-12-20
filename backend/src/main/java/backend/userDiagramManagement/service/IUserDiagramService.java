package backend.userDiagramManagement.service;

import backend.userDiagramManagement.dto.DiagramInfoDto;
import backend.userDiagramManagement.dto.create.DiagramCreateRequestDto;
import backend.userDiagramManagement.dto.search.DiagramSearchRequestDto;
import backend.userDiagramManagement.dto.share.DiagramShareRequestDto;
import backend.userDiagramManagement.dto.share.DiagramShareResponseDto;
import backend.userDiagramManagement.dto.update.DiagramUpdateRequestDto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Date;
import java.util.UUID;

public interface IUserDiagramService {
    DiagramInfoDto createDiagram(int userId, DiagramCreateRequestDto request);

    void deleteDiagram(int userId, UUID id);

    Page<DiagramInfoDto> searchDiagrams(int userId, DiagramSearchRequestDto request, Pageable pageable);

    DiagramShareResponseDto shareDiagram(int userId, UUID diagramId, DiagramShareRequestDto request);

    Page<DiagramInfoDto> getDiagramsByUserId(int userId, Pageable pageable);

	 Date updateDiagram(int id, DiagramUpdateRequestDto request, UUID id2);
}
