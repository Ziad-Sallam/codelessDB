package backend.publicDiagramManagement.service;

import backend.entities.User;
import backend.entities.publicDiagramEntities.PublicDiagram;
import backend.publicDiagramManagement.dto.PublicDiagramInfoDto;
import backend.publicDiagramManagement.dto.user.PublicUserDto;
import backend.publicDiagramManagement.repository.PublicDiagramRepository;
import backend.user.UserRepository;
import backend.user.exceptions.UserException;
import backend.userDiagramManagement.dto.ContributorDto;
import backend.userDiagramManagement.repository.DiagramRepository;
import backend.userDiagramManagement.service.UserDiagramService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PublicUserService {

    private final UserRepository userRepository;
    private final DiagramRepository diagramRepository;
    private final PublicDiagramRepository publicDiagramRepository;
    private final UserDiagramService userDiagramService;


    public PublicUserDto getDesignerProfile(String userName) {

        User user = userRepository.findByUsername(userName);
        if (user == null)
            throw new UserException.UserNotFoundException("No user found with username " + userName);

        Long publicCount = diagramRepository.countPublicDiagramsByOwner(user.getId());
        Long totalStars = diagramRepository.sumStarsOfPublicDiagramsByOwner(user.getId());

        return PublicUserDto.builder()
                .name(user.getPublicProfile())
                .username(user.getUsername())
                .bio(user.getBio())
                .picture(user.getPicture())
                .email(user.getEmail())
                .url(user.getProfileWebsiteUrl())
                .publicCount(publicCount)
                .totalStars(totalStars)
                .createdAt(user.getCreatedAt())
                .build();
    }

    public Page<PublicDiagramInfoDto> getPublicDiagrams(String userName, Pageable pageable) {

        Page<PublicDiagram> page = publicDiagramRepository.findPublicDiagramsByOwner(
                userName,
                pageable
        );

        return page.map(publicDiagram -> {
            List<ContributorDto> contributors = userDiagramService.getContributors(publicDiagram.getId());
            return PublicDiagramInfoDto.toDto(publicDiagram, contributors);
        });
    }

    public Page<PublicDiagramInfoDto> getStaredPublicDiagrams(String userName, Pageable pageable) {

        Page<PublicDiagram> page = publicDiagramRepository.findStaredPublicDiagramsByUser(
                userName,
                pageable
        );

        return page.map(publicDiagram -> {
            List<ContributorDto> contributors = userDiagramService.getContributors(publicDiagram.getId());
            return PublicDiagramInfoDto.toDto(publicDiagram, contributors);
        });
    }
}
