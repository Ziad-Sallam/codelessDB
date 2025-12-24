package backend.userDiagramManagement.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import backend.entities.Diagram;
import backend.entities.User;
import backend.entities.joins.UserDiagram;
import backend.entities.joins.UserDiagramId;
import backend.user.Role;
import backend.user.UserRepository;
import backend.user.exceptions.UserException;
import backend.userDiagramManagement.dto.ContributorDto;
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
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserDiagramService implements IUserDiagramService {
    private static final LocalDateTime MIN_DATE = LocalDateTime.of(1970, 1, 1, 0, 0, 0);
    private static final LocalDateTime MAX_DATE = LocalDateTime.of(2100, 12, 31, 23, 59, 59);

    private final DiagramRepository diagramRepository;
    private final UserRepository userRepository;
    private final UserDiagramRepository userDiagramRepository;

    public User getUserOrThrow(int userId) {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new UserException.UserNotFoundException("User not found");
        }
        return user;
    }

    public Diagram getDiagramOrThrow(UUID diagramId) {
        return diagramRepository.findById(diagramId)
                .orElseThrow(() -> new DiagramException.DiagramNotFoundException(
                        "Diagram with id " + diagramId + " not found"));
    }

    public UserDiagram getUserDiagramOrThrow(int userId, UUID diagramId) {
        return userDiagramRepository.findByUser_IdAndDiagram_Id(userId, diagramId)
                .orElseThrow(() -> new DiagramException.PermissionDeniedException(
                        "User does not have permission for diagram " + diagramId));
    }

    public List<ContributorDto> getContributors(UUID diagramId) {
        return userDiagramRepository.findByDiagram_Id(diagramId)
                .stream()
                .map(ud -> new ContributorDto(
                        ud.getUser().getUsername(),
                        ud.getUser().getPicture(),
                        ud.getRole()))
                .toList();
    }

    public void checkOwner(UserDiagram userDiagram, String action) {
        if (userDiagram.getRole() != Role.OWNER) {
            throw new DiagramException.PermissionDeniedException(
                    "Only the owner can " + action + ", user's role is " + userDiagram.getRole());
        }
    }

    @Override
    @Transactional
    public Page<DiagramInfoDto> getDiagramsByUserId(int userId, Pageable pageable) {
        getUserOrThrow(userId);
        return userDiagramRepository
                .findByUser_Id(userId, pageable)
                .map(ud -> DiagramInfoDto.toDto(
                        ud.getDiagram(),
                        ud.getRole(),
                        getContributors(ud.getDiagram().getId()),
                        ud.getDiagram().getPublicDiagram() != null));
    }

    @Override
    @Transactional
    public DiagramInfoDto createDiagram(int userId, DiagramCreateRequestDto request) {
        User user = getUserOrThrow(userId);
        Diagram diagram = diagramRepository.save(request.toDiagram());

        UserDiagram join = UserDiagram.builder()
                .UUID(UserDiagramId.builder().userId(userId).diagramId(diagram.getId()).build())
                .user(user)
                .diagram(diagram)
                .role(Role.OWNER)
                .build();

        userDiagramRepository.save(join);
        return DiagramInfoDto.toDto(diagram, Role.OWNER, getContributors(diagram.getId()), false);
    }

    @Transactional
    public LocalDateTime updateDiagram(int userId, DiagramUpdateRequestDto request, UUID diagramId) {
        getUserOrThrow(userId);
        Diagram diagram = getDiagramOrThrow(diagramId);
        UserDiagram userDiagram = getUserDiagramOrThrow(userId, diagramId);

		if (userDiagram.getRole() == Role.READER) {
			throw new DiagramException.PermissionDeniedException("Only the owner can update this diagram");
		}

		if (request.getName() != null) {
			diagram.setName(request.getName());
		}
		if (request.getJsonContent() != null) {
			diagram.setContent(request.getJsonContent());
		}
		if (request.getThumbnail() != null) {
			diagram.setThumbnail(request.getThumbnail());
		}
		return diagramRepository.save(diagram).getLastModified();
	}

    @Override
    @Transactional
    public void deleteDiagram(int userId, UUID diagramId) {
        getUserOrThrow(userId);

        UserDiagram userDiagram = getUserDiagramOrThrow(userId, diagramId);

        userDiagramRepository.delete(userDiagram);

        if (userDiagram.getRole() == Role.OWNER) {
            if (userDiagram.getDiagram().getPublicDiagram() != null)
                throw new DiagramException.PermissionDeniedException(
                        "Can't delete public diagram, Unpublished it first");

            UserDiagram anyOne = userDiagramRepository.findFirstByDiagram_Id(diagramId);

            if (anyOne != null) {
                anyOne.setRole(Role.OWNER);
                userDiagramRepository.save(anyOne);
            }
        }

        boolean hasUsers = userDiagramRepository.existsByDiagram_Id(diagramId);

        if (!hasUsers) {
            Diagram diagram = getDiagramOrThrow(diagramId);
            diagramRepository.delete(diagram);
        }
    }

    @Override
    @Transactional
    public DiagramDto searchDiagramById(int userId, UUID diagramId) {
        getUserOrThrow(userId);
        Diagram diagram = getDiagramOrThrow(diagramId);
        UserDiagram userDiagram = getUserDiagramOrThrow(userId, diagramId);
        return DiagramDto.toDto(diagram, userDiagram.getRole());
    }

    @Override
    @Transactional
    public Page<DiagramInfoDto> searchDiagrams(int userId,
            DiagramSearchRequestDto request,
            Pageable pageable) {

        getUserOrThrow(userId);

        String nameFilter = Optional
                .ofNullable(request.getName())
                .orElse("");

        LocalDateTime startDate = Optional
                .ofNullable(request.getStart())
                .orElse(MIN_DATE);

        LocalDateTime endDate = Optional
                .ofNullable(request.getEnd())
                .orElse(MAX_DATE);

        return userDiagramRepository
                .findAllByUser_IdAndDiagram_NameContainingIgnoreCaseAndDiagram_CreatedAtBetween(
                        userId,
                        nameFilter,
                        startDate,
                        endDate,
                        pageable)
                .map(ud -> DiagramInfoDto.toDto(
                        ud.getDiagram(),
                        ud.getRole(),
                        getContributors(ud.getDiagram().getId()),
                        ud.getDiagram().getPublicDiagram() != null));
    }

    @Override
    @Transactional
    public DiagramShareResponseDto shareDiagram(int userId, UUID diagramId, DiagramShareRequestDto request) {
        getUserOrThrow(userId);
        Diagram diagram = getDiagramOrThrow(diagramId);
        UserDiagram ownerLink = getUserDiagramOrThrow(userId, diagram.getId());
        checkOwner(ownerLink, "share this diagram");

        if (request.getRole() == Role.OWNER) {
            throw new DiagramException.InvalidDiagramDataException("There must be exactly one owner per diagram.");
        }

        User targetUser = userRepository.findByUsername(request.getToUserName());
        if (targetUser == null) {
            throw new UserException.UserNotFoundException("Shared-to user not found: " + request.getToUserName());
        }

        Optional<UserDiagram> optionalLink = userDiagramRepository.findByUser_IdAndDiagram_Id(targetUser.getId(),
                diagram.getId());

        if (request.isDelete()) {
            if (optionalLink.isEmpty()) {
                throw new DiagramException.InvalidDiagramDataException(
                        "The user does not have any permission on this diagram.");
            }

            UserDiagram link = optionalLink.get();

            if (link.getRole() == Role.OWNER) {
                throw new DiagramException.InvalidDiagramDataException("The owner cannot be removed from the diagram.");
            }

            userDiagramRepository.delete(link);

            return new DiagramShareResponseDto(
                    "Diagram role deleted successfully",
                    targetUser.getUsername(),
                    null,
                    null);
        }

        if (optionalLink.isEmpty()) {

            UserDiagram join = UserDiagram.builder()
                    .UUID(UserDiagramId.builder()
                            .userId(targetUser.getId())
                            .diagramId(diagram.getId())
                            .build())
                    .user(targetUser)
                    .diagram(diagram)
                    .role(request.getRole())
                    .build();

            userDiagramRepository.save(join);

            return new DiagramShareResponseDto(
                    "Diagram shared successfully",
                    targetUser.getUsername(),
                    request.getRole(),
                    targetUser.getPicture());
        }

        UserDiagram link = optionalLink.get();

        if (link.getRole() == request.getRole()) {
            throw new DiagramException.InvalidDiagramDataException(
                    "Diagram is already shared with user " + targetUser.getUsername() + " with the same role.");
        }

        link.setRole(request.getRole());
        userDiagramRepository.save(link);

        return new DiagramShareResponseDto(
                "Diagram role updated successfully",
                targetUser.getUsername(),
                request.getRole(),
                targetUser.getPicture());
    }

    @Override
    @Transactional
    public void updateDDL(UUID diagramId, String ddl) {
        Diagram diagram = getDiagramOrThrow(diagramId);

        if (ddl == null) {
            throw new DiagramException.InvalidDiagramDataException("DDL cannot be null");
        }

        diagram.setDdl(ddl);
        diagramRepository.save(diagram);
    }

    @Override
    public String getDDL(UUID diagramId) {
        Diagram diagram = getDiagramOrThrow(diagramId);
        return diagram.getDdl();
    }
}
