package backend.publicDiagramManagement.PublucDiagramServiceTest;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import backend.entities.Diagram;
import backend.entities.joins.UserDiagram;
import backend.entities.publicDiagramEntities.Hashtag;
import backend.entities.publicDiagramEntities.PublicDiagram;
import backend.publicDiagramManagement.dto.DiagramCannedQueryDto;
import backend.publicDiagramManagement.dto.publish.PublishDiagramRequestDto;
import backend.publicDiagramManagement.exceptions.PublicDiagramException;
import backend.publicDiagramManagement.repository.PublicDiagramRepository;
import backend.publicDiagramManagement.service.HashtagService;
import backend.publicDiagramManagement.service.PublicDiagramServiceImpl;
import backend.user.Role;
import backend.userDiagramManagement.service.UserDiagramService;

@ExtendWith(MockitoExtension.class)
class UpdateTests {

  @Mock
  private PublicDiagramRepository publicDiagramRepository;
  @Mock
  private UserDiagramService userDiagramService;
  @Mock
  private HashtagService hashtagService;

  @InjectMocks
  private PublicDiagramServiceImpl service;

  private PublishDiagramRequestDto dto;
  private UUID diagramId;
  private Diagram diagram;
  private UserDiagram userDiagram;

  @BeforeEach
  void setup() {
    diagramId = UUID.randomUUID();

    dto = new PublishDiagramRequestDto();
    dto.setDiagramId(diagramId);
    dto.setShortDescription("updated short");
    dto.setDetailedDescription("updated detailed");
    dto.setHashTags(List.of("#sql", "#erd"));
    dto.setCannedQueries(List.of(new DiagramCannedQueryDto("SELECT 1", "Q1", "desc", 1)));

    diagram = Diagram.builder().id(diagramId).build();

    userDiagram = UserDiagram.builder()
        .diagram(diagram)
        .role(Role.OWNER)
        .build();
  }

  @Test
  void updatePublicDiagram_success() {
    when(userDiagramService.getUserDiagramOrThrow(anyInt(), eq(diagramId)))
        .thenReturn(userDiagram);

    PublicDiagram existing = new PublicDiagram();
    existing.setId(diagramId);
    existing.setDiagram(diagram);
    existing.setShortDescription("old short");

    when(publicDiagramRepository.findById(diagramId))
        .thenReturn(Optional.of(existing));

    when(hashtagService.resolveHashtags(any()))
        .thenReturn(Set.of(new Hashtag(1, "sql", new HashSet<>())));

    service.updatePublicDiagram(1, dto);

    assertEquals("updated short", existing.getShortDescription());
    assertEquals("updated detailed", existing.getDetailedDescription());
    verify(publicDiagramRepository, times(1)).save(existing);
    verify(userDiagramService, times(1)).checkOwner(userDiagram, "update public details");
  }

  @Test
  void updatePublicDiagram_diagramNotFound_throws() {
    when(userDiagramService.getUserDiagramOrThrow(anyInt(), eq(diagramId)))
        .thenThrow(new PublicDiagramException.DiagramNotFoundException("not found"));

    assertThrows(PublicDiagramException.DiagramNotFoundException.class,
        () -> service.updatePublicDiagram(1, dto));
  }

  @Test
  void updatePublicDiagram_notOwner_throws() {
    when(userDiagramService.getUserDiagramOrThrow(anyInt(), eq(diagramId)))
        .thenReturn(userDiagram);

    doThrow(new PublicDiagramException.PermissionDeniedException("not owner"))
        .when(userDiagramService).checkOwner(eq(userDiagram), anyString());

    assertThrows(PublicDiagramException.PermissionDeniedException.class,
        () -> service.updatePublicDiagram(1, dto));
  }

  @Test
  void updatePublicDiagram_nullCannedQueries_throwsNPE() {
    dto.setCannedQueries(null);

    when(userDiagramService.getUserDiagramOrThrow(anyInt(), eq(diagramId)))
        .thenReturn(userDiagram);

    assertThrows(NullPointerException.class,
        () -> service.updatePublicDiagram(1, dto));
  }
}
