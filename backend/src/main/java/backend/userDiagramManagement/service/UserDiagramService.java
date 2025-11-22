package backend.userDiagramManagement.service;

import backend.entities.Diagram;
import backend.entities.User;
import backend.entities.joins.UserDiagram;
import backend.entities.joins.UserDiagramId;
import backend.user.Role;
import backend.user.UserRepository;
import backend.user.exceptions.UserException;
import backend.userDiagramManagement.dto.DiagramInfoDto;
import backend.userDiagramManagement.dto.create.DiagramCreateRequestDto;
import backend.userDiagramManagement.dto.get.DiagramGetInfoRequestDto;
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
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserDiagramService implements IUserDiagramService {

    private final DiagramRepository diagramRepository;
    private final UserRepository userRepository;
    private final UserDiagramRepository userDiagramRepository;

    private User getUserOrThrow(int userId) {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new UserException.UserNotFoundException("User not found");
        }
        return user;
    }

    private Diagram getDiagramOrThrow(UUID diagramId) {
        return diagramRepository.findById(diagramId)
                .orElseThrow(() -> new DiagramException.DiagramNotFoundException(
                        "Diagram with id " + diagramId + " not found"));
    }

    private UserDiagram getUserDiagramOrThrow(int userId, UUID diagramId) {
        return userDiagramRepository.findByUser_IdAndDiagram_Id(userId, diagramId)
                .orElseThrow(() -> new DiagramException.PermissionDeniedException(
                        "User does not have permission for diagram " + diagramId));
    }

    private List<DiagramInfoDto.Contributor> getContributors(UUID diagramId) {
        return userDiagramRepository.findByDiagram_Id(diagramId)
                .stream()
                .map(ud -> new DiagramInfoDto.Contributor(
                            ud.getUser().getUsername(),
                            ud.getRole()
                        )
                ).toList();
    }

    private void checkOwner(UserDiagram userDiagram, String action) {
        if (userDiagram.getRole() != Role.OWNER) {
            throw new DiagramException.PermissionDeniedException(
                    "Only the owner can " + action + ", user's role is " + userDiagram.getRole());
        }
    }

    private Date parseDateOrDefault(String dateStr, String defaultDate) {
        return dateStr != null ? Date.valueOf(dateStr) : Date.valueOf(defaultDate);
    }

    @Override
    public Page<DiagramInfoDto> getDiagramsByUserId(int userId, DiagramGetInfoRequestDto request, Pageable pageable) {
        getUserOrThrow(userId);
        return userDiagramRepository
                .findByUser_Id(userId, pageable)
                .map(ud -> DiagramInfoDto.toDto(
                            ud.getDiagram(),
                            ud.getRole(),
                            getContributors(ud.getDiagram().getId())
                        )
                );
    }

    @Override
    public UUID createDiagram(int userId, DiagramCreateRequestDto request) {
        User user = getUserOrThrow(userId);
        Diagram diagram = diagramRepository.save(request.toDiagram());

        UserDiagram join = UserDiagram.builder()
                .UUID(UserDiagramId.builder().userId(userId).diagramId(diagram.getId()).build())
                .user(user)
                .diagram(diagram)
                .role(Role.OWNER)
                .build();

        userDiagramRepository.save(join);
        return diagram.getId();
    }

    @Override
    public Date updateDiagram(int userId, DiagramUpdateRequestDto request, UUID diagramId) {
        getUserOrThrow(userId);
        Diagram diagram = getDiagramOrThrow(diagramId);
        UserDiagram userDiagram = getUserDiagramOrThrow(userId, diagramId);

        if (userDiagram.getRole() == Role.READER)
            throw new DiagramException.PermissionDeniedException("Only the owner can update this diagram");

        if (request.getName() != null) diagram.setName(request.getName());
        if (request.getJsonContent() != null) diagram.setContent(request.getJsonContent());

        return diagramRepository.save(diagram).getLastModified();
    }

    @Override
    public void deleteDiagram(int userId, UUID diagramId) {
        getUserOrThrow(userId);
        UserDiagram userDiagram = getUserDiagramOrThrow(userId, diagramId);

        userDiagramRepository.delete(userDiagram);

        if (!userDiagramRepository.existsByDiagram_Id(diagramId))
            diagramRepository.delete(getDiagramOrThrow(diagramId));
    }

    @Override
    public DiagramDto searchDiagramById(int userId, UUID diagramId) {
        getUserOrThrow(userId);
        Diagram diagram = getDiagramOrThrow(diagramId);
        UserDiagram userDiagram = getUserDiagramOrThrow(userId, diagramId);
        return DiagramDto.toDto(diagram, userDiagram.getRole());
    }

    @Override
    public Page<DiagramDto> searchDiagrams(int userId, DiagramSearchRequestDto request, Pageable pageable) {
        getUserOrThrow(userId);

        String nameFilter = request.getName() != null ? request.getName() : "";
        Date startDate = parseDateOrDefault(request.getStart(), "1970-01-01");
        Date endDate = parseDateOrDefault(request.getEnd(), "2100-12-31");

        return userDiagramRepository
                .findAllByUser_IdAndDiagram_NameContainingIgnoreCaseAndDiagram_CreatedAtBetween(
                        userId,
                        nameFilter,
                        startDate,
                        endDate,
                        pageable
                ).map(ud -> DiagramDto.toDto(ud.getDiagram(), ud.getRole()));
    }

    @Override
    public DiagramShareResponseDto shareDiagram(int userId, DiagramShareRequestDto request) {
        getUserOrThrow(userId);
        Diagram diagram = getDiagramOrThrow(request.getDiagramId());
        UserDiagram ownerLink = getUserDiagramOrThrow(userId, diagram.getId());
        checkOwner(ownerLink, "share this diagram");

        User targetUser = userRepository.findByUsername(request.getToUserName());
        if (targetUser == null)
            throw new UserException.UserNotFoundException("Target user not found: " + request.getToUserName());

        if (userDiagramRepository.existsByUser_IdAndDiagram_Id(targetUser.getId(), diagram.getId()))
            throw new DiagramException.InvalidDiagramDataException("Diagram is already shared with user " + targetUser.getUsername());

        UserDiagram join = UserDiagram.builder()
                .UUID(UserDiagramId.builder().userId(targetUser.getId()).diagramId(diagram.getId()).build())
                .user(targetUser)
                .diagram(diagram)
                .role(request.getRole())
                .build();

        userDiagramRepository.save(join);

        return new DiagramShareResponseDto("Diagram shared successfully", targetUser.getUsername(), request.getRole());
    }
}
