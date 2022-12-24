package ccc.keeweapi.controller.api.user;

import ccc.keeweapi.dto.ApiResponse;
import ccc.keeweapi.dto.user.FollowToggleResponse;
import ccc.keeweapi.dto.user.ProfileMyPageResponse;
import ccc.keeweapi.dto.user.OnboardRequest;
import ccc.keeweapi.dto.user.OnboardResponse;
import ccc.keeweapi.dto.user.UploadProfilePhotoResponse;
import ccc.keeweapi.service.user.ProfileApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/user/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileApiService profileApiService;

    @PostMapping
    public ApiResponse<OnboardResponse> onboard(@RequestBody @Valid OnboardRequest request) {
        return ApiResponse.ok(profileApiService.onboard(request));
    }

    @PostMapping("/follow/{targetId}")
    public ApiResponse<FollowToggleResponse> toggleFollowership(@PathVariable Long targetId) {
        return ApiResponse.ok(profileApiService.toggleFollowership(targetId));
    }

    @PostMapping("/photo")
    public ApiResponse<UploadProfilePhotoResponse> uploadProfilePhoto(@RequestParam("image") MultipartFile imageFile) {
        return ApiResponse.ok(profileApiService.uploadProfilePhoto(imageFile));
    }

    @GetMapping("/{targetId}")
    public ApiResponse<ProfileMyPageResponse> getMyPageProfile(@PathVariable Long targetId) {
        return ApiResponse.ok(profileApiService.getMyPageProfile(targetId));
    }
}
