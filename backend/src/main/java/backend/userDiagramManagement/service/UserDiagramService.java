package backend.userDiagramManagement.service;

import backend.entities.Diagram;
import backend.entities.User;
import backend.entities.joins.UserDiagram;
import backend.entities.joins.UserDiagramId;
import backend.user.Role;
import backend.user.UserRepository;
import backend.user.exceptions.UserException;
import backend.userDiagramManagement.dto.create.DiagramCreateRequestDto;
import backend.userDiagramManagement.dto.search.DiagramSearchRequestDto;
import backend.userDiagramManagement.dto.share.DiagramShareResponseDto;
import backend.userDiagramManagement.dto.update.DiagramUpdateRequestDto;
import backend.userDiagramManagement.dto.share.DiagramShareRequestDto;
import backend.userDiagramManagement.dto.DiagramDto;
import backend.userDiagramManagement.exceptions.DiagramException;
import backend.userDiagramManagement.repository.DiagramRepository;
import backend.userDiagramManagement.repository.UserDiagramRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.sql.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserDiagramService implements IUserDiagramService {

    private final DiagramRepository diagramRepository;
    private final UserRepository userRepository;
    private final UserDiagramRepository userDiagramRepository;

    private void assertUserExists(int userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserException.UserNotFoundException("User not found");
        }
    }

    @Override
    public UUID createDiagram(int userId, DiagramCreateRequestDto request) {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new UserException.UserNotFoundException("User not found");
        }

        Diagram diagram = request.toDiagram();
        diagram = diagramRepository.save(diagram);

        UserDiagramId joinId = UserDiagramId
                .builder()
                .userId(userId)
                .diagramId(diagram.getId())
                .build();

        UserDiagram join = UserDiagram
                .builder()
                .UUID(joinId)
                .user(user)
                .diagram(diagram)
                .role(Role.OWNER)
                .build();

        userDiagramRepository.save(join);
        return diagram.getId();
    }

    @Override
    public Date updateDiagram(int userId, DiagramUpdateRequestDto request) {
        assertUserExists(userId);

        Diagram diagram = diagramRepository.findById(request.getId())
                .orElseThrow(() -> new DiagramException.DiagramNotFoundException(
                        "Diagram with id " + request.getId() + " not found"));

        UserDiagram userDiagram = userDiagramRepository
                .findByUser_IdAndDiagram_Id(userId, diagram.getId())
                .orElseThrow(() -> new DiagramException.PermissionDeniedException(
                        "User does not have permission to update this diagram"));

        if (userDiagram.getRole() != Role.OWNER) {
            throw new DiagramException.PermissionDeniedException(
                    "Only the owner can update this diagram, the user's role is " +  userDiagram.getRole());
        }

        if (request.getName() != null)
            diagram.setName(request.getName());

        if (request.getJsonContent() != null)
            diagram.setContent(request.getJsonContent());

        if (request.getThumbnail() != null)
            diagram.setThumbnail(request.getThumbnail());

        Diagram updatedDiagram = diagramRepository.save(diagram);
        return updatedDiagram.getLastModified();
    }

    @Override
    public void deleteDiagram(int userId, UUID id) {
        assertUserExists(userId);

        Diagram diagram = diagramRepository.findById(id)
                .orElseThrow(() -> new DiagramException.DiagramNotFoundException(
                        "Diagram with id " + id + " not found"));

        UserDiagram userDiagram = userDiagramRepository
                .findByUser_IdAndDiagram_Id(userId, id)
                .orElseThrow(() -> new DiagramException.PermissionDeniedException(
                        "User does not have permission to delete this diagram"));

        if (userDiagram.getRole() != Role.OWNER) {
            throw new DiagramException.PermissionDeniedException(
                    "Only the owner can update this diagram, the user's role is " +  userDiagram.getRole());
        }

        diagramRepository.delete(diagram);
    }

    @Override
    public DiagramDto searchDiagramById(int userId, UUID id) {
        assertUserExists(userId);

        Diagram diagram = diagramRepository.findById(id)
                .orElseThrow(() -> new DiagramException.DiagramNotFoundException(
                        "Diagram with id " + id + " not found"));

        UserDiagram userDiagram = userDiagramRepository
                .findByUser_IdAndDiagram_Id(userId, id)
                .orElseThrow(() -> new DiagramException.PermissionDeniedException(
                        "User does not have permission to delete this diagram"));

        return DiagramDto.toDto(diagram, userDiagram.getRole());
    }

    @Override
    public Page<DiagramDto> searchDiagrams(int userId, DiagramSearchRequestDto request, Pageable pageable) {
        if (!userRepository.existsById(userId)) {
            throw new UserException.UserNotFoundException("User not found");
        }

        String nameFilter = request.getName() != null ? request.getName() : "";

        Date startDate = null;
        Date endDate = null;

        try {
            if (request.getStart() != null)
                startDate = java.sql.Date.valueOf(request.getStart());
            if (request.getEnd() != null)
                endDate = java.sql.Date.valueOf(request.getEnd());
        } catch (IllegalArgumentException e) {
            throw new DiagramException.InvalidDiagramDataException("Invalid date format. Use yyyy-MM-dd.");
        }

        if (startDate == null) {
            startDate = java.sql.Date.valueOf("1970-01-01");
        }
        if (endDate == null) {
            endDate = java.sql.Date.valueOf("2100-12-31");
        }

        Page<UserDiagram> userDiagramsPage = userDiagramRepository
                .findAllByUser_IdAndDiagram_NameContainingIgnoreCaseAndDiagram_CreatedAtBetween(
                        userId,
                        nameFilter,
                        startDate,
                        endDate,
                        pageable
                );

        return userDiagramsPage.map(userDiagram -> DiagramDto.toDto(userDiagram.getDiagram(), userDiagram.getRole()));
    }

    @Override
    public DiagramShareResponseDto shareDiagram(int userId, DiagramShareRequestDto request) {
        if (!userRepository.existsById(userId)) {
            throw new UserException.UserNotFoundException("User not found");
        }

        Diagram diagram = diagramRepository.findById(request.getDiagramId())
                .orElseThrow(() -> new DiagramException.DiagramNotFoundException(
                        "Diagram not found with id " + request.getDiagramId()));

        UserDiagram ownerLink = userDiagramRepository
                .findByUser_IdAndDiagram_Id(userId, request.getDiagramId())
                .orElseThrow(() -> new DiagramException.PermissionDeniedException(
                        "You don't have permission to share this diagram"));

        if (ownerLink.getRole() != Role.OWNER) {
            throw new DiagramException.PermissionDeniedException(
                    "Only the owner can share this diagram");
        }

        User targetUser = userRepository.findByUsername(request.getToUserName());
        if (targetUser == null) {
            throw new UserException.UserNotFoundException(
                    "Target user not found: " + request.getToUserName());
        }

        boolean alreadyShared = userDiagramRepository
                .existsByUser_IdAndDiagram_Id(targetUser.getId(), diagram.getId());
        if (alreadyShared) {
            throw new DiagramException.InvalidDiagramDataException(
                    "Diagram is already shared with user " + targetUser.getUsername());
        }

        UserDiagramId joinId = UserDiagramId.builder()
                .userId(targetUser.getId())
                .diagramId(diagram.getId())
                .build();

        UserDiagram join = UserDiagram.builder()
                .UUID(joinId)
                .user(targetUser)
                .diagram(diagram)
                .role(Role.valueOf(request.getRole()))
                .build();

        userDiagramRepository.save(join);

        return new DiagramShareResponseDto(
                "Diagram shared successfully",
                targetUser.getUsername(),
                request.getRole()
        );
    }
}
