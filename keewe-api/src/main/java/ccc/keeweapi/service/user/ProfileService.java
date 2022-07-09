package ccc.keeweapi.service.user;

import ccc.keeweapi.dto.user.CreateLinkDto;
import ccc.keeweapi.dto.user.NicknameCreateRequestDto;
import ccc.keeweapi.dto.user.NicknameCreateResponseDto;
import ccc.keewedomain.domain.user.Profile;
import ccc.keewedomain.repository.user.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ProfileService {

    private final ProfileRepository profileRepository;

    @Transactional
    public Long createLink(CreateLinkDto createLinkDto) {
        String link = createLinkDto.getLink();
        Profile profile = profileRepository.findByIdAndUserIdAndDeletedFalseOrElseThrow(createLinkDto.getProfileId(), 5L);

        checkDuplicateLinkOrElseThrows(link);

        profile.createLink(link);

        return 0L;
    }

    @Transactional
    public NicknameCreateResponseDto createNickname(NicknameCreateRequestDto nicknameCreateDto, Long userId) {
        String nickname = nicknameCreateDto.getNickname();

        Profile profile = profileRepository.findByIdAndUserIdAndDeletedFalseOrElseThrow(
                nicknameCreateDto.getProfileId(),
                userId
        );

        profile.createNickname(nickname);

        return NicknameCreateResponseDto.builder()
                .nickname(nickname)
                .status(profile.getProfileStatus())
                .build();
    }


    private void checkDuplicateLinkOrElseThrows(String link) {
        if (profileRepository.existsByLinkAndDeletedFalse(link))
            throw new IllegalArgumentException("허보성 바보!!");
    }
}