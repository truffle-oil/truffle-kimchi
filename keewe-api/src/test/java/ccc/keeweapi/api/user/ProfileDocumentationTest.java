package ccc.keeweapi.api.user;

import ccc.keeweapi.config.security.UserDetailService;
import ccc.keeweapi.config.security.UserPrincipal;
import ccc.keeweapi.document.utils.RestDocsTestSupport;
import ccc.keeweapi.dto.common.LinkDto;
import ccc.keeweapi.dto.user.*;
import ccc.keeweapi.service.user.ProfileService;
import ccc.keeweapi.utils.SecurityUtil;
import ccc.keewedomain.domain.common.enums.Activity;
import ccc.keewedomain.domain.common.enums.LinkType;
import ccc.keewedomain.domain.user.Profile;
import ccc.keewedomain.domain.user.User;
import ccc.keewedomain.domain.user.enums.ProfileStatus;
import ccc.keewedomain.domain.user.enums.UserStatus;
import ccc.keewedomain.service.ProfileDomainService;
import ccc.keewedomain.service.SocialLinkDomainService;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.List;

import static ccc.keewedomain.domain.common.enums.Activity.*;
import static ccc.keewedomain.domain.user.enums.ProfileStatus.*;
import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ProfileDocumentationTest extends RestDocsTestSupport {

    @MockBean
    private ProfileService profileService;

    @MockBean
    private ProfileDomainService profileDomainService;

    @MockBean
    private UserDetailService userDetailsService;

    @MockBean
    SocialLinkDomainService socialLinkDomainService;


    @Test
    @DisplayName("닉네임 생성 API")
    void create_nickname_test() throws Exception {
        // given

        String email = "test@keewe.com";
        String nickname = "\uD83C\uDDF0\uD83C\uDDF7\uD83D\uDE011한글B❣️✅#️⃣";
        Long profileId = 1L;

        User user = User.builder()
                .id(1L)
                .email(email)
                .status(UserStatus.ACTIVE)
                .deleted(false)
                .build();

        NicknameCreateRequest requestDto = new NicknameCreateRequest();
        requestDto.setNickname(nickname);
        requestDto.setProfileId(profileId);
        String token = jwtUtils.createToken(email, new ArrayList<>());


        when(userDetailsService.loadUserByUsername(any()))
                .thenReturn(new UserPrincipal(user));

        when(profileService.createNickname(any()))
                .thenReturn(NicknameCreateResponse.of(nickname, ACTIVITIES_NEEDED));


        mockMvc.perform(
                        post("/api/v1/profiles/nickname")
                                .with(user(new UserPrincipal(user)))
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .content(objectMapper.writeValueAsString(requestDto))
                                .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andDo(restDocs.document(
                        resource(
                                ResourceSnippetParameters.builder()
                                        .description("Profile 온보딩 닉네임 생성 API 입니다.")
                                        .summary("닉네임 생성 API 입니다.")
                                        .requestHeaders(
                                                headerWithName("Authorization").description("유저의 JWT"))
                                        .requestFields(
                                                fieldWithPath("nickname").description("생성할 닉네임"),
                                                fieldWithPath("profileId").description("대상 프로필의 id"))
                                        .responseFields(
                                                fieldWithPath("message").description("요청 결과 메세지"),
                                                fieldWithPath("code").description("결과 코드"),
                                                fieldWithPath("data.nickname").description("생성된 닉네임"),
                                                fieldWithPath("data.status")
                                                        .description("요청 완료 후 해당 프로필의 상태")
                                                        .type("ENUM")
                                                        .attributes(key("enumValues").value(List.of(ProfileStatus.values()))))
                                        .tag("Profile")
                                        .build()
                        )));
    }

    @Test
    @DisplayName("링크 생성 api")
    void create_link_test() throws Exception {
        String email = "test@keewe.com";
        String link = "my._.link";
        Long profileId = 0L;

        User user = User.builder().build();

        LinkCreateRequest requestDto = new LinkCreateRequest(profileId, link);
        String token = jwtUtils.createToken(email, new ArrayList<>());

        when(userDetailsService.loadUserByUsername(any()))
                .thenReturn(new UserPrincipal(user));

        when(profileService.createLink(any()))
                .thenReturn(new LinkCreateResponse(link, SOCIAL_LINK_NEEDED));


        mockMvc.perform(
                        post("/api/v1/profiles/link")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .content(objectMapper.writeValueAsString(requestDto))
                                .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andDo(restDocs.document(
                        resource(
                                ResourceSnippetParameters.builder()
                                        .description("Profile 온보딩 링크 생성 API 입니다.")
                                        .summary("링크 생성 API입니다.")
                                        .requestHeaders(
                                                headerWithName("Authorization").description("유저의 JWT"))
                                        .requestFields(
                                                fieldWithPath("link").description("생성할 링크"),
                                                fieldWithPath("profileId").description("대상 프로필의 id"))
                                        .responseFields(
                                                fieldWithPath("message").description("요청 결과 메세지"),
                                                fieldWithPath("code").description("결과 코드"),
                                                fieldWithPath("data.link").description("생성된 링크"),
                                                fieldWithPath("data.status")
                                                        .description("요청 완료 후 해당 프로필의 상태")
                                                        .type("ENUM")
                                                        .attributes(key("enumValues").value(List.of(ProfileStatus.values()))))
                                        .tag("Profile")
                                        .build()
                        )));
    }

    @Test
    @DisplayName("활동 분야 등록 api")
    void create_activities_test() throws Exception {
        String email = "test@keewe.com";
        String token = jwtUtils.createToken(email, new ArrayList<>());
        List<Activity> activities = List.of(INDIE, POP);

        User user = User.builder().build();
        ActivitiesCreateRequest requestDto = new ActivitiesCreateRequest(user.getId(), activities);
        when(userDetailsService.loadUserByUsername(any()))
                .thenReturn(new UserPrincipal(user));

        when(profileService.createActivities(any()))
                .thenReturn(ActivitiesCreateResponse.of(activities, LINK_NEEDED));

        mockMvc.perform(
                        post("/api/v1/profiles/activities")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .content(objectMapper.writeValueAsString(requestDto))
                                .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andDo(restDocs.document(
                        resource(
                                ResourceSnippetParameters.builder()
                                        .description("Profile 온보딩 활동 분야 생성 API 입니다.")
                                        .summary("활동 분야 생성 API입니다.")
                                        .requestHeaders(
                                                headerWithName("Authorization").description("유저의 JWT"))
                                        .requestFields(
                                                fieldWithPath("activities")
                                                        .description("생성할 활동 분야의 리스트")
                                                        .type("array")
                                                        .attributes(key("enumValues").value(List.of(Activity.values()))),
                                                fieldWithPath("profileId").description("대상 프로필의 id"))
                                        .responseFields(
                                                fieldWithPath("message").description("요청 결과 메세지"),
                                                fieldWithPath("code").description("결과 코드"),
                                                fieldWithPath("data.activities")
                                                        .description("활동 생성 결과")
                                                        .type("array")
                                                        .attributes(key("enumValues").value(List.of(Activity.values()))),
                                                fieldWithPath("data.status")
                                                        .description("요청 완료 후 해당 프로필의 상태")
                                                        .type("ENUM")
                                                        .attributes(key("enumValues").value(List.of(ProfileStatus.values())))
                                        )
                                        .tag("Profile")
                                        .build()
                        )));
    }

    @Test
    @DisplayName("활동 분야 검색 api")
    void search_activities_test() throws Exception {
        String email = "test@keewe.com";
        String token = jwtUtils.createToken(email, new ArrayList<>());

        User user = User.builder().build();
        when(userDetailsService.loadUserByUsername(any()))
                .thenReturn(new UserPrincipal(user));

        when(profileService.searchActivities(any()))
                .thenReturn(new ActivitiesSearchResponse(List.of(OTHER_MUSIC)));

        mockMvc.perform(
                        get("/api/v1/profiles/activities")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .param("keyword", "음악")
                ).andExpect(status().isOk())
                .andDo(restDocs.document(
                        resource(
                                ResourceSnippetParameters.builder()
                                        .description("Profile 온보딩 활동 분야 검색 API 입니다.")
                                        .summary("활동 분야 검색 API 입니다.")
                                        .requestHeaders(
                                                headerWithName("Authorization").description("유저의 JWT"))
                                        .requestParameters(
                                                parameterWithName("keyword").description("검색어")
                                        )
                                        .responseFields(
                                                fieldWithPath("message").description("요청 결과 메세지"),
                                                fieldWithPath("code").description("결과 코드"),
                                                fieldWithPath("data.activities")
                                                        .description("검색 결과")
                                                        .type("array")
                                                        .attributes(key("enumValues").value(List.of(Activity.values()))))
                                        .tag("Profile")
                                        .build()
                        )));

    }

    @Test
    @DisplayName("소셜 링크 등록 API")
    void create_social_links_test() throws Exception {
        String email = "test@keewe.com";
        Long profileId = 1L;
        String token = jwtUtils.createToken(email, new ArrayList<>());

        User user = User.builder()
                .id(1L)
                .email(email)
                .status(UserStatus.ACTIVE)
                .deleted(false)
                .build();

        when(userDetailsService.loadUserByUsername(any()))
                .thenReturn(new UserPrincipal(user));

        LinkDto linkDto1 = new LinkDto();
        linkDto1.setUrl("https://www.youtube.com/hello");
        linkDto1.setType("YOUTUBE");

        LinkDto linkDto2 = new LinkDto();
        linkDto2.setUrl("https://facebook.com/world");
        linkDto2.setType("FACEBOOK");

        List<LinkDto> linkDtos = new ArrayList<>();
        linkDtos.add(linkDto1);
        linkDtos.add(linkDto2);


        SocialLinkCreateRequest requestDto = new SocialLinkCreateRequest();
        requestDto.setProfileId(profileId);
        requestDto.setLinks(linkDtos);

        MockedStatic<SecurityUtil> socialLinkMockedStatic = Mockito.mockStatic(SecurityUtil.class);
        socialLinkMockedStatic.when(SecurityUtil::getUser).thenReturn(user);

        mockMvc.perform(
                        post("/api/v1/profiles/social-links")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .content(objectMapper.writeValueAsString(requestDto))
                                .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andDo(restDocs.document(
                        resource(
                                ResourceSnippetParameters.builder()
                                        .description("Profile 온보딩 소셜 링크 생성 API 입니다.")
                                        .summary("소셜 링크 생성 API 입니다.")
                                        .requestHeaders(
                                                headerWithName("Authorization").description("유저의 JWT"))
                                        .requestFields(
                                                fieldWithPath("profileId").description("대상 프로필의 id"),
                                                fieldWithPath("links[].url").description("등록할 주소"),
                                                fieldWithPath("links[].type")
                                                        .description("등록할 주소의 타입")
                                                        .type("ENUM")
                                                        .attributes(key("enumValues").value(List.of(LinkType.values()))))
                                        .responseFields(
                                                fieldWithPath("message").description("요청 결과 메세지"),
                                                fieldWithPath("code").description("결과 코드"),
                                                fieldWithPath("data").description("비어 있음"))
                                        .tag("Profile")
                                        .build()
                        )));
    }

    @Test
    @DisplayName("온보딩 미수행 프로필 조회 api")
    void incomplete_profile_select() throws Exception {
        when(userDetailsService.loadUserByUsername(any())).thenReturn(new UserPrincipal(User.builder().build()));
        when(profileService.getIncompleteProfile()).thenReturn(IncompleteProfileResponse.getExistResult(Profile.builder()
                        .id(1L)
                        .profileStatus(ProfileStatus.NICKNAME_NEEDED)
                        .build()));

        mockMvc.perform(get("/api/v1/profiles/incomplete")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtUtils.createToken("h0song@naver.com", List.of()))
                ).andExpect(status().isOk())
                .andDo(restDocs.document(
                        resource(
                                ResourceSnippetParameters.builder()
                                        .description("Profile 온보딩 미수행 프로필 검색 API 입니다.")
                                        .summary("온보딩 미수행 프로필 검색 API 입니다.")
                                        .requestHeaders(
                                                headerWithName("Authorization").description("유저의 JWT"))
                                        .responseFields(
                                                fieldWithPath("message").description("요청 결과 메세지"),
                                                fieldWithPath("code").description("결과 코드"),
                                                fieldWithPath("data.exist").description("미완성 프로필 존재 여부"),
                                                fieldWithPath("data.profileId").description("미완성 프로필 ID"),
                                                fieldWithPath("data.status").description("온보딩 수행해야 할 단계")
                                                        .type("ENUM")
                                                        .attributes(key("enumValues").value(List.of(ProfileStatus.values()))))
                                        .tag("Profile")
                                        .build()
                        )));

    }
}
