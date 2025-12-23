package backend.userDiagramManagment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import backend.entities.Diagram;
import backend.entities.User;
import backend.entities.joins.UserDiagram;
import backend.entities.joins.UserDiagramId;
import backend.entities.publicDiagramEntities.PublicDiagram;
import backend.user.Role;
import backend.user.UserRepository;
import backend.user.exceptions.UserException;
import backend.userDiagramManagement.dto.DiagramDto;
import backend.userDiagramManagement.dto.DiagramInfoDto;
import backend.userDiagramManagement.dto.create.DiagramCreateRequestDto;
import backend.userDiagramManagement.dto.search.DiagramSearchRequestDto;
import backend.userDiagramManagement.dto.share.DiagramShareRequestDto;
import backend.userDiagramManagement.dto.share.DiagramShareResponseDto;
import backend.userDiagramManagement.dto.update.DiagramUpdateRequestDto;
import backend.userDiagramManagement.exceptions.DiagramException;
import backend.userDiagramManagement.repository.DiagramRepository;
import backend.userDiagramManagement.repository.UserDiagramRepository;
import backend.userDiagramManagement.service.UserDiagramService;

class UserDiagramServiceTest {

        @Mock
        private DiagramRepository diagramRepository;
        @Mock
        private UserRepository userRepository;
        @Mock
        private UserDiagramRepository userDiagramRepository;

        @InjectMocks
        private UserDiagramService service;

        private User user;
        private Diagram diagram;
        private UserDiagram ownerLink;

        @BeforeEach
        void setup() {
                MockitoAnnotations.openMocks(this);

                user = new User();
                user.setId(1);
                user.setUsername("john");
                user.setPicture("john.png");

                diagram = Diagram.builder()
                                .id(UUID.randomUUID())
                                .name("Test Diagram")
                                .content(new byte[0])
                                .thumbnail("thumb.png")
                                .lastModified(LocalDateTime.now())
                                .createdAt(LocalDateTime.now())
                                .build();

                ownerLink = UserDiagram.builder()
                                .UUID(new UserDiagramId(1, diagram.getId()))
                                .user(user)
                                .diagram(diagram)
                                .role(Role.OWNER)
                                .build();
        }

        // ------------------------------------------------------------
        // GET DIAGRAMS BY USER
        // ------------------------------------------------------------
        @Test
        void getDiagramsByUserId_success() {
                when(userRepository.findById(1)).thenReturn(user);
                when(userDiagramRepository.findByUser_Id(eq(1), any(Pageable.class)))
                                .thenReturn(new PageImpl<>(List.of(ownerLink)));

                Page<DiagramInfoDto> result = service.getDiagramsByUserId(1, PageRequest.of(0, 10));

                assertEquals(1, result.getContent().size());
                DiagramInfoDto dto = result.getContent().get(0);
                assertEquals(diagram.getId(), dto.getDiagramId());
                assertEquals("Test Diagram", dto.getName());
                assertEquals("thumb.png", dto.getThumbnail());
                assertEquals(Role.OWNER, dto.getRole());
        }

        // ------------------------------------------------------------
        // CREATE DIAGRAM
        // ------------------------------------------------------------
        @Test
        void createDiagram_success() {
                when(userRepository.findById(1)).thenReturn(user);
                when(diagramRepository.save(any(Diagram.class))).thenReturn(diagram);
                when(userDiagramRepository.save(any(UserDiagram.class))).thenReturn(ownerLink);
                when(userDiagramRepository.findByDiagram_Id(diagram.getId())).thenReturn(List.of(ownerLink));

                DiagramCreateRequestDto request = new DiagramCreateRequestDto();
                request.setThumbnail("thumb.png");

                DiagramInfoDto result = service.createDiagram(1, request);

                assertNotNull(result.getDiagramId());
                assertEquals("Test Diagram", result.getName());
                verify(userDiagramRepository, times(1)).save(any(UserDiagram.class));
        }

        @Test
        void createDiagram_userNotFound() {
                when(userRepository.findById(1)).thenReturn(null);

                assertThrows(UserException.UserNotFoundException.class,
                                () -> service.createDiagram(1, new DiagramCreateRequestDto()));
        }

        // ------------------------------------------------------------
        // UPDATE DIAGRAM
        // ------------------------------------------------------------
        @Test
        void updateDiagram_success() {
                DiagramUpdateRequestDto request = new DiagramUpdateRequestDto();
                request.setName("Updated");
                request.setJsonContent(new byte[0]);

                when(userRepository.findById(1)).thenReturn(user);
                when(diagramRepository.findById(diagram.getId())).thenReturn(Optional.of(diagram));
                when(userDiagramRepository.findByUser_IdAndDiagram_Id(1, diagram.getId()))
                                .thenReturn(Optional.of(ownerLink));
                when(diagramRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

                LocalDateTime updated = service.updateDiagram(1, request, diagram.getId());

                assertNotNull(updated);
                assertEquals("Updated", diagram.getName());
                assertArrayEquals(new byte[0], diagram.getContent());
        }

        @Test
        void updateDiagram_forbiddenForReader() {
                ownerLink.setRole(Role.READER);

                when(userRepository.findById(1)).thenReturn(user);
                when(diagramRepository.findById(diagram.getId())).thenReturn(Optional.of(diagram));
                when(userDiagramRepository.findByUser_IdAndDiagram_Id(1, diagram.getId()))
                                .thenReturn(Optional.of(ownerLink));

                assertThrows(DiagramException.PermissionDeniedException.class,
                                () -> service.updateDiagram(1, new DiagramUpdateRequestDto(), diagram.getId()));
        }

        // ------------------------------------------------------------
        // DELETE DIAGRAM
        // ------------------------------------------------------------
        @Test
        void deleteDiagram_lastUser_deletesDiagram() {
                when(userRepository.findById(1)).thenReturn(user);
                when(userDiagramRepository.findByUser_IdAndDiagram_Id(1, diagram.getId()))
                                .thenReturn(Optional.of(ownerLink));
                // simulate no other users after deletion
                when(userDiagramRepository.findFirstByDiagram_Id(diagram.getId())).thenReturn(null);
                when(userDiagramRepository.existsByDiagram_Id(diagram.getId())).thenReturn(false);
                when(diagramRepository.findById(diagram.getId())).thenReturn(Optional.of(diagram));

                service.deleteDiagram(1, diagram.getId());

                verify(userDiagramRepository).delete(ownerLink);
                verify(diagramRepository).delete(diagram);
        }

        @Test
        void deleteDiagram_hasOtherUsers_notDeleteDiagram() {
                when(userRepository.findById(1)).thenReturn(user);
                when(userDiagramRepository.findByUser_IdAndDiagram_Id(1, diagram.getId()))
                                .thenReturn(Optional.of(ownerLink));
                // simulate someone else still exists
                when(userDiagramRepository.existsByDiagram_Id(diagram.getId())).thenReturn(true);

                service.deleteDiagram(1, diagram.getId());

                verify(userDiagramRepository).delete(ownerLink);
                verify(diagramRepository, never()).delete(any());
        }

        // ------------------------------------------------------------
        // SEARCH BY ID
        // ------------------------------------------------------------
        @Test
        void searchById_success() {
                when(userRepository.findById(1)).thenReturn(user);
                when(diagramRepository.findById(diagram.getId())).thenReturn(Optional.of(diagram));
                when(userDiagramRepository.findByUser_IdAndDiagram_Id(1, diagram.getId()))
                                .thenReturn(Optional.of(ownerLink));

                DiagramDto dto = service.searchDiagramById(1, diagram.getId());

                assertEquals(diagram.getId(), dto.getId());
                assertEquals("Test Diagram", dto.getName());
                assertArrayEquals(new byte[0], dto.getContent());
                assertEquals(Role.OWNER, dto.getRole());
        }

        // ------------------------------------------------------------
        // SEARCH WITH FILTER
        // ------------------------------------------------------------
        @Test
        void searchDiagrams_success() {
                when(userRepository.findById(1)).thenReturn(user);

                // when searching, repository returns a page with one UserDiagram
                when(userDiagramRepository
                                .findAllByUser_IdAndDiagram_NameContainingIgnoreCaseAndDiagram_CreatedAtBetween(
                                                eq(1), anyString(), any(), any(), any(Pageable.class)))
                                .thenReturn(new PageImpl<>(List.of(ownerLink)));

                // getContributors uses findByDiagram_Id
                when(userDiagramRepository.findByDiagram_Id(diagram.getId()))
                                .thenReturn(List.of(ownerLink));

                DiagramSearchRequestDto request = new DiagramSearchRequestDto();
                Page<DiagramInfoDto> result = service.searchDiagrams(1, request, Pageable.unpaged());

                assertEquals(1, result.getContent().size());
                DiagramInfoDto dto = result.getContent().get(0);
                assertEquals(diagram.getId(), dto.getDiagramId());
                assertEquals("Test Diagram", dto.getName());
                assertEquals("thumb.png", dto.getThumbnail());
                assertEquals(Role.OWNER, dto.getRole());
                assertEquals(1, dto.getContributorDtos().size());
                assertEquals("john", dto.getContributorDtos().get(0).getName());
        }

        @Test
        void shareDiagram_success() {
                User target = new User();
                target.setId(2);
                target.setUsername("mike");
                target.setPicture("mike.png");

                DiagramShareRequestDto req = new DiagramShareRequestDto();
                req.setToUserName("mike");
                req.setRole(Role.WRITER);
                req.setDelete(false);

                when(userRepository.findById(1)).thenReturn(user);
                when(diagramRepository.findById(diagram.getId())).thenReturn(Optional.of(diagram));
                when(userDiagramRepository.findByUser_IdAndDiagram_Id(1, diagram.getId()))
                                .thenReturn(Optional.of(ownerLink));
                when(userRepository.findByUsername("mike")).thenReturn(target);
                when(userDiagramRepository.findByUser_IdAndDiagram_Id(2, diagram.getId()))
                                .thenReturn(Optional.empty());
                when(userDiagramRepository.save(any(UserDiagram.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                DiagramShareResponseDto res = service.shareDiagram(1, diagram.getId(), req);

                assertEquals("Diagram shared successfully", res.getMessage());
                assertEquals("mike", res.getSharedWith());
                assertEquals(Role.WRITER, res.getRole());
        }

        @Test
        void shareDiagram_targetAlreadyHasAccess_sameRole_throws() {
                DiagramShareRequestDto req = new DiagramShareRequestDto();
                req.setToUserName("mike");
                req.setRole(Role.WRITER);
                req.setDelete(false);

                User target = new User();
                target.setId(2);
                target.setUsername("mike");

                UserDiagram existing = UserDiagram.builder()
                                .UUID(new UserDiagramId(2, diagram.getId()))
                                .user(target)
                                .diagram(diagram)
                                .role(Role.WRITER)
                                .build();

                when(userRepository.findById(1)).thenReturn(user);
                when(diagramRepository.findById(diagram.getId())).thenReturn(Optional.of(diagram));
                when(userDiagramRepository.findByUser_IdAndDiagram_Id(1, diagram.getId()))
                                .thenReturn(Optional.of(ownerLink));
                when(userRepository.findByUsername("mike")).thenReturn(target);
                when(userDiagramRepository.findByUser_IdAndDiagram_Id(2, diagram.getId()))
                                .thenReturn(Optional.of(existing));

                assertThrows(DiagramException.InvalidDiagramDataException.class,
                                () -> service.shareDiagram(1, diagram.getId(), req));
        }

        // ------------------------------------------------------------
        // UPDATE DDL
        // ------------------------------------------------------------
        @Test
        void updateDDL_success() {
                String newDdl = "CREATE TABLE test (...)";
                when(diagramRepository.findById(diagram.getId())).thenReturn(Optional.of(diagram));
                when(diagramRepository.save(any(Diagram.class))).thenAnswer(invocation -> invocation.getArgument(0));

                service.updateDDL(diagram.getId(), newDdl);

                assertEquals(newDdl, diagram.getDdl());
                verify(diagramRepository).save(diagram);
        }

        @Test
        void updateDDL_null_throws() {
                when(diagramRepository.findById(diagram.getId())).thenReturn(Optional.of(diagram));

                assertThrows(DiagramException.InvalidDiagramDataException.class,
                                () -> service.updateDDL(diagram.getId(), null));
        }

        // ------------------------------------------------------------
        // SHARE DIAGRAM (UPDATES & DELETIONS)
        // ------------------------------------------------------------
        @Test
        void shareDiagram_updateExistingRole_success() {
                User target = new User();
                target.setId(2);
                target.setUsername("mike");

                UserDiagram existingLink = UserDiagram.builder()
                                .UUID(new UserDiagramId(2, diagram.getId()))
                                .user(target)
                                .diagram(diagram)
                                .role(Role.READER)
                                .build();

                DiagramShareRequestDto req = new DiagramShareRequestDto();
                req.setToUserName("mike");
                req.setRole(Role.WRITER);
                req.setDelete(false);

                when(userRepository.findById(1)).thenReturn(user);
                when(diagramRepository.findById(diagram.getId())).thenReturn(Optional.of(diagram));
                when(userDiagramRepository.findByUser_IdAndDiagram_Id(1, diagram.getId()))
                                .thenReturn(Optional.of(ownerLink));
                when(userRepository.findByUsername("mike")).thenReturn(target);
                when(userDiagramRepository.findByUser_IdAndDiagram_Id(2, diagram.getId()))
                                .thenReturn(Optional.of(existingLink));

                DiagramShareResponseDto res = service.shareDiagram(1, diagram.getId(), req);

                assertEquals("Diagram role updated successfully", res.getMessage());
                assertEquals(Role.WRITER, existingLink.getRole());
                verify(userDiagramRepository).save(existingLink);
        }

        @Test
        void shareDiagram_deleteRole_success() {
                User target = new User();
                target.setId(2);
                target.setUsername("mike");

                UserDiagram existingLink = UserDiagram.builder()
                                .UUID(new UserDiagramId(2, diagram.getId()))
                                .user(target)
                                .diagram(diagram)
                                .role(Role.WRITER)
                                .build();

                DiagramShareRequestDto req = new DiagramShareRequestDto();
                req.setToUserName("mike");
                req.setDelete(true);

                when(userRepository.findById(1)).thenReturn(user);
                when(diagramRepository.findById(diagram.getId())).thenReturn(Optional.of(diagram));
                when(userDiagramRepository.findByUser_IdAndDiagram_Id(1, diagram.getId()))
                                .thenReturn(Optional.of(ownerLink));
                when(userRepository.findByUsername("mike")).thenReturn(target);
                when(userDiagramRepository.findByUser_IdAndDiagram_Id(2, diagram.getId()))
                                .thenReturn(Optional.of(existingLink));

                DiagramShareResponseDto res = service.shareDiagram(1, diagram.getId(), req);

                assertEquals("Diagram role deleted successfully", res.getMessage());
                verify(userDiagramRepository).delete(existingLink);
        }

        @Test
        void shareDiagram_deleteRole_userHasNoPermission_throws() {
                User target = new User();
                target.setId(2);
                target.setUsername("mike");

                DiagramShareRequestDto req = new DiagramShareRequestDto();
                req.setToUserName("mike");
                req.setDelete(true);

                when(userRepository.findById(1)).thenReturn(user);
                when(diagramRepository.findById(diagram.getId())).thenReturn(Optional.of(diagram));
                when(userDiagramRepository.findByUser_IdAndDiagram_Id(1, diagram.getId()))
                                .thenReturn(Optional.of(ownerLink));
                when(userRepository.findByUsername("mike")).thenReturn(target);
                when(userDiagramRepository.findByUser_IdAndDiagram_Id(2, diagram.getId()))
                                .thenReturn(Optional.empty());

                assertThrows(DiagramException.InvalidDiagramDataException.class,
                                () -> service.shareDiagram(1, diagram.getId(), req));
        }

        @Test
        void shareDiagram_deleteRole_isOwner_throws() {
                User target = new User();
                target.setId(2);
                target.setUsername("mike");

                UserDiagram targetLink = UserDiagram.builder()
                                .UUID(new UserDiagramId(2, diagram.getId()))
                                .user(target)
                                .diagram(diagram)
                                .role(Role.OWNER)
                                .build();

                DiagramShareRequestDto req = new DiagramShareRequestDto();
                req.setToUserName("mike");
                req.setDelete(true);

                when(userRepository.findById(1)).thenReturn(user);
                when(diagramRepository.findById(diagram.getId())).thenReturn(Optional.of(diagram));
                when(userDiagramRepository.findByUser_IdAndDiagram_Id(1, diagram.getId()))
                                .thenReturn(Optional.of(ownerLink));
                when(userRepository.findByUsername("mike")).thenReturn(target);
                when(userDiagramRepository.findByUser_IdAndDiagram_Id(2, diagram.getId()))
                                .thenReturn(Optional.of(targetLink));

                assertThrows(DiagramException.InvalidDiagramDataException.class,
                                () -> service.shareDiagram(1, diagram.getId(), req));
        }

        @Test
        void shareDiagram_shareWithRoleOwner_throws() {
                DiagramShareRequestDto req = new DiagramShareRequestDto();
                req.setToUserName("mike");
                req.setRole(Role.OWNER);

                when(userRepository.findById(1)).thenReturn(user);
                when(diagramRepository.findById(diagram.getId())).thenReturn(Optional.of(diagram));
                when(userDiagramRepository.findByUser_IdAndDiagram_Id(1, diagram.getId()))
                                .thenReturn(Optional.of(ownerLink));

                assertThrows(DiagramException.InvalidDiagramDataException.class,
                                () -> service.shareDiagram(1, diagram.getId(), req));
        }

        @Test
        void shareDiagram_targetUserNotFound_throws() {
                DiagramShareRequestDto req = new DiagramShareRequestDto();
                req.setToUserName("nonexistent");

                when(userRepository.findById(1)).thenReturn(user);
                when(diagramRepository.findById(diagram.getId())).thenReturn(Optional.of(diagram));
                when(userDiagramRepository.findByUser_IdAndDiagram_Id(1, diagram.getId()))
                                .thenReturn(Optional.of(ownerLink));
                when(userRepository.findByUsername("nonexistent")).thenReturn(null);

                assertThrows(UserException.UserNotFoundException.class,
                                () -> service.shareDiagram(1, diagram.getId(), req));
        }

        @Test
        void deleteDiagram_publicDiagram_throws() {
                diagram.setPublicDiagram(new PublicDiagram());

                when(userRepository.findById(1)).thenReturn(user);
                when(userDiagramRepository.findByUser_IdAndDiagram_Id(1, diagram.getId()))
                                .thenReturn(Optional.of(ownerLink));

                assertThrows(DiagramException.PermissionDeniedException.class,
                                () -> service.deleteDiagram(1, diagram.getId()));
        }

        @Test
        void deleteDiagram_ownerLeaves_assignsToAnother() {
                User mike = new User();
                mike.setId(2);
                UserDiagram mikeLink = UserDiagram.builder()
                                .UUID(new UserDiagramId(2, diagram.getId()))
                                .user(mike)
                                .diagram(diagram)
                                .role(Role.WRITER)
                                .build();

                when(userRepository.findById(1)).thenReturn(user);
                when(userDiagramRepository.findByUser_IdAndDiagram_Id(1, diagram.getId()))
                                .thenReturn(Optional.of(ownerLink));
                when(userDiagramRepository.findFirstByDiagram_Id(diagram.getId())).thenReturn(mikeLink);
                when(userDiagramRepository.existsByDiagram_Id(diagram.getId())).thenReturn(true);

                service.deleteDiagram(1, diagram.getId());

                verify(userDiagramRepository).delete(ownerLink);
                assertEquals(Role.OWNER, mikeLink.getRole());
                verify(userDiagramRepository).save(mikeLink);
        }

        @Test
        void updateDiagram_updateThumbnail_success() {
                DiagramUpdateRequestDto request = new DiagramUpdateRequestDto();
                request.setThumbnail("new_thumbnail.png");

                when(userRepository.findById(1)).thenReturn(user);
                when(diagramRepository.findById(diagram.getId())).thenReturn(Optional.of(diagram));
                when(userDiagramRepository.findByUser_IdAndDiagram_Id(1, diagram.getId()))
                                .thenReturn(Optional.of(ownerLink));
                when(diagramRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

                service.updateDiagram(1, request, diagram.getId());

                assertEquals("new_thumbnail.png", diagram.getThumbnail());
        }

        @Test
        void updateDiagram_diagramNotFound_throws() {
                when(userRepository.findById(1)).thenReturn(user);
                when(diagramRepository.findById(diagram.getId())).thenReturn(Optional.empty());

                assertThrows(DiagramException.DiagramNotFoundException.class,
                                () -> service.updateDiagram(1, new DiagramUpdateRequestDto(), diagram.getId()));
        }

        @Test
        void checkOwner_notOwner_throws() {
                ownerLink.setRole(Role.WRITER);
                assertThrows(DiagramException.PermissionDeniedException.class,
                                () -> service.checkOwner(ownerLink, "any action"));
        }

        @Test
        void getUserDiagramOrThrow_success() {
                when(userDiagramRepository.findByUser_IdAndDiagram_Id(1, diagram.getId()))
                                .thenReturn(Optional.of(ownerLink));

                UserDiagram result = service.getUserDiagramOrThrow(1, diagram.getId());

                assertNotNull(result);
                assertEquals(ownerLink, result);
        }

        @Test
        void getUserDiagramOrThrow_notFound_throws() {
                when(userDiagramRepository.findByUser_IdAndDiagram_Id(1, diagram.getId()))
                                .thenReturn(Optional.empty());

                assertThrows(DiagramException.PermissionDeniedException.class,
                                () -> service.getUserDiagramOrThrow(1, diagram.getId()));
        }
}
