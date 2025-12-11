package backend.publicDiagramManagement.service;


import backend.publicDiagramManagement.dto.PublicDiagramDto;
import backend.publicDiagramManagement.dto.PublicDiagramInfoDto;
import backend.publicDiagramManagement.dto.get.ToBePublishedDiagramDto;
import backend.publicDiagramManagement.dto.publish.PublishDiagramRequestDto;
import backend.publicDiagramManagement.dto.search.SearchRequestDto;
import backend.publicDiagramManagement.dto.user.PublicUserInfoDto;
import backend.userDiagramManagement.dto.DiagramInfoDto;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface PublicDiagramService {

    void publishDiagram(int userId, PublishDiagramRequestDto publishDiagramDto);

    PublicDiagramDto viewPublicDiagram(int userId, UUID diagramId);

    void forkPublicDiagram(int userId, UUID diagramId);

    Page<ToBePublishedDiagramDto> getToBePublishedDiagrams(int userId, Pageable pageable);

    void starPublicDiagram(int userId, UUID diagramId);

    void unstarPublicDiagram(int userId, UUID diagramId);

    Page<PublicDiagramInfoDto> getForkedPublicDiagrams(int userId, Pageable pageable);

    Page<PublicDiagramInfoDto> getStaredPublicDiagrams(int userId, Pageable pageable);

    Page<PublicDiagramInfoDto> searchPublicDiagrams(SearchRequestDto searchRequestDto, Pageable pageable);

    Page<PublicUserInfoDto> searchUsersByPublicDiagrams(SearchRequestDto searchRequestDto, Pageable pageable);

    void unPublishPublicDiagram(int userId, @NonNull UUID diagramId);
}
