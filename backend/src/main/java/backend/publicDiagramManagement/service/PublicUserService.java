package backend.publicDiagramManagement.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import backend.entities.User;
import backend.entities.publicDiagramEntities.PublicDiagram;
import backend.publicDiagramManagement.dto.PublicDiagramInfoDto;
import backend.publicDiagramManagement.dto.user.PublicUserDto;
import backend.publicDiagramManagement.dto.user.PublicUserFollowDto;
import backend.publicDiagramManagement.repository.PublicDiagramRepository;
import backend.user.UserRepository;
import backend.user.exceptions.UserException;
import backend.userDiagramManagement.dto.ContributorDto;
import backend.userDiagramManagement.repository.DiagramRepository;
import backend.userDiagramManagement.service.UserDiagramService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PublicUserService {

    private final UserRepository userRepository;
    private final DiagramRepository diagramRepository;
    private final PublicDiagramRepository publicDiagramRepository;
    private final UserDiagramService userDiagramService;

    private User findUserByIfOrThrew(int userId) {
        User user = userRepository.findById(userId);
        if (user == null)
            throw new UserException.UserNotFoundException("No user found with id " + userId);
        return user;
    }

    private User findUserByUsernameOrThrew(String userName) {
        User user = userRepository.findByUsername(userName);
        if (user == null)
            throw new UserException.UserNotFoundException("No user found with username " + userName);
        return user;
    }

    public PublicUserDto getDesignerProfile(Integer currentUserId, String userName) {

        User user = userRepository.findByUsername(userName);
        if (user == null)
            throw new UserException.UserNotFoundException("No user found with username " + userName);

        Long publicCount = diagramRepository.countPublicDiagramsByOwner(user.getId());
        Long totalStars = user.getTotalStars();
        long starredCount = publicDiagramRepository.countStaredPublicDiagramsByUser(userName);

        boolean isFollowed = false;
        if (currentUserId != null) {
            isFollowed = userRepository.countFollowing(currentUserId, user.getId()) > 0;
        }

        return PublicUserDto.builder()
                .name(user.getPublicProfile() != null && !user.getPublicProfile().isBlank() ? user.getPublicProfile() : user.getUsername())
                .username(user.getUsername())
                .bio(user.getBio())
                .picture(user.getPicture())
                .email(user.getEmail())
                .url(user.getProfileWebsiteUrl())
                .publicCount(publicCount == null ? 0 : publicCount)
                .totalStars(totalStars == null ? 0 : totalStars)
                .starredCount(starredCount)
                .createdAt(user.getCreatedAt())
                .isFollowed(isFollowed)
                .followersCount(user.getFollowersCount())
                .followingCount(user.getFollowingCount())
                .build();
    }

    public Page<PublicDiagramInfoDto> getPublicDiagrams(String userName, Pageable pageable) {

        Page<PublicDiagram> page = publicDiagramRepository.findPublicDiagramsByOwner(
                userName,
                pageable);

        return page.map(publicDiagram -> {
            List<ContributorDto> contributors = userDiagramService.getContributors(publicDiagram.getId());
            return PublicDiagramInfoDto.toDto(publicDiagram, contributors);
        });
    }

    public Page<PublicDiagramInfoDto> getStaredPublicDiagrams(String userName, Pageable pageable) {

        Page<PublicDiagram> page = publicDiagramRepository.findStaredPublicDiagramsByUser(
                userName,
                pageable);

        return page.map(publicDiagram -> {
            List<ContributorDto> contributors = userDiagramService.getContributors(publicDiagram.getId());
            return PublicDiagramInfoDto.toDto(publicDiagram, contributors);
        });
    }

    public void followUser(int userId, @NonNull String userName) {
        User user = findUserByIfOrThrew(userId);
        User userToFollow = findUserByUsernameOrThrew(userName);
        
        if (user.getId() == userToFollow.getId())
            throw new UserException.UserCantFollowHimselfException("User can't follow himself");
        if (userRepository.countFollowing(user.getId(), userToFollow.getId()) > 0)
            throw new UserException.UserAlreadyFollowedException("User " + user.getUsername() + " is already following " + userToFollow.getUsername());

        userRepository.addFollower(userToFollow.getId(), user.getId());
        userRepository.addFollowing(user.getId(), userToFollow.getId());

        user.setFollowingCount(user.getFollowingCount() + 1);
        userToFollow.setFollowersCount(userToFollow.getFollowersCount() + 1);
        userRepository.save(user);
        userRepository.save(userToFollow);
    }

    public void unfollowUser(int userId, @NonNull String userName) {
        User user = findUserByIfOrThrew(userId);
        User userToFollow = findUserByUsernameOrThrew(userName);

        if (userRepository.countFollowing(user.getId(), userToFollow.getId()) == 0)
            throw new UserException.UserAlreadyFollowedException("User " + user.getUsername() + " is not following " + userToFollow.getUsername());

        userRepository.removeFollower(userToFollow.getId(), user.getId());
        userRepository.removeFollowing(user.getId(), userToFollow.getId());

        user.setFollowingCount(Math.max(0, user.getFollowingCount() - 1));
        userToFollow.setFollowersCount(Math.max(0, userToFollow.getFollowersCount() - 1));
        userRepository.save(user);
        userRepository.save(userToFollow);
    }

    public Page<PublicUserFollowDto> getFollowersByUsername(String userName, Pageable pageable) {
        User user = findUserByUsernameOrThrew(userName);
        return getFollowers(user.getId(), pageable);
    }

    public Page<PublicUserFollowDto> getFollowingsByUsername(String userName, Pageable pageable) {
        User user = findUserByUsernameOrThrew(userName);
        return getFollowings(user.getId(), pageable);
    }

    public Page<PublicUserFollowDto> getFollowers(int userId, Pageable pageable) {
        Page<Object[]> raw = userRepository.findFollowersWithStats(userId, pageable);

        return raw.map(row -> {
            User user = (User) row[0];

            Long publicCount = row[1] == null ? 0L : ((Number) row[1]).longValue();
            Long totalStars = row[2] == null ? 0L : ((Number) row[2]).longValue();
            Long score = row[3] == null ? 0L : ((Number) row[3]).longValue();

            return PublicUserFollowDto.toDto(user, publicCount, totalStars, score);
        });
    }

    public Page<PublicUserFollowDto> getFollowings(int userId, Pageable pageable) {
        Page<Object[]> raw = userRepository.findFollowingWithStats(userId, pageable);

        return raw.map(row -> {
            User user = (User) row[0];

            Long publicCount = row[1] == null ? 0L : ((Number) row[1]).longValue();
            Long totalStars = row[2] == null ? 0L : ((Number) row[2]).longValue();
            Long score = row[3] == null ? 0L : ((Number) row[3]).longValue();

            return PublicUserFollowDto.toDto(user, publicCount, totalStars, score);
        });
    }
}
