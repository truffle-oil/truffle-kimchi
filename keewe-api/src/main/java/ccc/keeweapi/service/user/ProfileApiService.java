package ccc.keeweapi.service.user;

import static ccc.keewecore.consts.KeeweConsts.MY_PAGE_TITLE_LIMIT;

import ccc.keeweapi.component.ProfileAssembler;
import ccc.keeweapi.dto.user.AllAchievedTitleResponse;
import ccc.keeweapi.dto.user.BlockUserResponse;
import ccc.keeweapi.dto.user.FollowToggleResponse;
import ccc.keeweapi.dto.user.FollowUserListResponse;
import ccc.keeweapi.dto.user.MyBlockUserListResponse;
import ccc.keeweapi.dto.user.MyPageTitleResponse;
import ccc.keeweapi.dto.user.OnboardRequest;
import ccc.keeweapi.dto.user.OnboardResponse;
import ccc.keeweapi.dto.user.ProfileMyPageResponse;
import ccc.keeweapi.dto.user.ProfileUpdateRequest;
import ccc.keeweapi.dto.user.ProfileUpdateResponse;
import ccc.keeweapi.dto.user.UnblockUserResponse;
import ccc.keeweapi.dto.user.UploadProfilePhotoResponse;
import ccc.keeweapi.utils.SecurityUtil;
import ccc.keewedomain.dto.user.FollowCheckDto;
import ccc.keewedomain.persistence.domain.title.TitleAchievement;
import ccc.keewedomain.persistence.domain.user.Block;
import ccc.keewedomain.persistence.domain.user.Follow;
import ccc.keewedomain.persistence.domain.user.User;
import ccc.keewedomain.persistence.repository.utils.CursorPageable;
import ccc.keewedomain.service.challenge.query.ChallengeParticipateQueryDomainService;
import ccc.keewedomain.service.user.ProfileDomainService;
import ccc.keewedomain.service.user.UserDomainService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


@Service
@RequiredArgsConstructor
public class ProfileApiService {

    private final ProfileDomainService profileDomainService;
    private final UserDomainService userDomainService;
    private final ChallengeParticipateQueryDomainService challengeParticipateQueryDomainService;
    private final ProfileAssembler profileAssembler;

    @Transactional
    public OnboardResponse onboard(OnboardRequest request) {
        User user = profileDomainService.onboard(profileAssembler.toOnboardDto(request));
        return profileAssembler.toOnboardResponse(user);
    }

    @Transactional
    public FollowToggleResponse toggleFollowership(Long targetId) {
        boolean following = profileDomainService.toggleFollowership(profileAssembler.toFollowToggleDto(targetId));
        return profileAssembler.toFollowToggleResponse(following);
    }

    @Transactional
    public UploadProfilePhotoResponse uploadProfilePhoto(MultipartFile imageFile) {
        String image = profileDomainService.uploadProfilePhoto(profileAssembler.toUploadProfilePhotoDto(imageFile));
        return UploadProfilePhotoResponse.of(image);
    }

    @Transactional(readOnly = true)
    public ProfileMyPageResponse getMyPageProfile(Long targetId) {
        User targetUser = userDomainService.getUserByIdWithInterests(targetId);
        Long userId = SecurityUtil.getUserId();

        boolean isFollowing = profileDomainService.getFollowingTargetIdSet(FollowCheckDto.of(userId, targetId));
        Long followerCount = profileDomainService.getFollowerCount(targetUser);
        Long followingCount = profileDomainService.getFollowingCount(targetUser);

        String challengeName = challengeParticipateQueryDomainService.findCurrentParticipationWithChallenge(targetId)
                .map(challengeParticipation -> challengeParticipation.getChallenge().getName())
                .orElse(null);

        return profileAssembler.toProfileMyPageResponse(targetUser, isFollowing, followerCount, followingCount, challengeName);
    }

    @Transactional(readOnly = true)
    public MyPageTitleResponse getMyPageTitles(Long userId) {
        List<TitleAchievement> titleAchievements = profileDomainService.getTitleAchievements(userId, MY_PAGE_TITLE_LIMIT);
        Long total = profileDomainService.getAchievedTitleCount(userId);

        return profileAssembler.toMyPageTitleResponse(total, titleAchievements);
    }

    @Transactional(readOnly = true)
    public AllAchievedTitleResponse getAllAchievedTitles(Long userId) {
        List<TitleAchievement> titleAchievements = profileDomainService.getTitleAchievements(userId, Integer.MAX_VALUE);

        return profileAssembler.toAllAchievedTitleResponse(SecurityUtil.getUser(), titleAchievements);
    }

    @Transactional(readOnly = true)
    public FollowUserListResponse getFollowers(Long userId, CursorPageable<LocalDateTime> cPage) {
        List<Follow> follows = profileDomainService.getFollowers(userId, cPage);

        return getFollowUser(follows, Follow::getFollower);
    }

    @Transactional(readOnly = true)
    public FollowUserListResponse getFollowees(Long userId, CursorPageable<LocalDateTime> cPage) {
        List<Follow> follows = profileDomainService.getFollowees(userId, cPage);

        return getFollowUser(follows, Follow::getFollowee);
    }

    @Transactional
    public BlockUserResponse blockUser(Long blockedUserId) {
        Long blockUserId = profileDomainService.blockUser(SecurityUtil.getUserId(), blockedUserId);
        return profileAssembler.toBlockUserResponse(blockedUserId);
    }

    private FollowUserListResponse getFollowUser(List<Follow> follows, Function<Follow, User> followUserFunction) {
        List<User> users = follows.stream()
                .map(followUserFunction)
                .collect(Collectors.toList());

        Set<Long> followingIdSet = profileDomainService.getFollowingTargetIdSet(SecurityUtil.getUser(), users);
        return profileAssembler.toFollowUserListResponse(follows, users, followingIdSet);
    }

    @Transactional
    public UnblockUserResponse unblockUser(Long blockedUserId) {
        Long unblockUserId = profileDomainService.unblockUser(SecurityUtil.getUserId(), blockedUserId);
        return profileAssembler.toUnblockUserResponse(unblockUserId);
    }

    @Transactional(readOnly = true)
    public MyBlockUserListResponse getMyBlockList() {
        List<Block> blocks = profileDomainService.findBlocksByUserId(SecurityUtil.getUserId());
        return profileAssembler.toMyBlockUserListResponse(blocks);
    }

    @Transactional
    public ProfileUpdateResponse updateProfile(ProfileUpdateRequest request) {
        User user = profileDomainService.updateProfile(SecurityUtil.getUserId(), profileAssembler.toProfileUpdateDto(request));
        return profileAssembler.toProfileUpdateResponse(user);
    }
}
