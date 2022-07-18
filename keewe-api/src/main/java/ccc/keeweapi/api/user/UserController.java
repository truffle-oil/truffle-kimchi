package ccc.keeweapi.api.user;

import ccc.keeweapi.dto.ApiResponse;
import ccc.keeweapi.service.user.UserApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {
    private final UserApiService userService;

    @GetMapping("/kakao")
    public ApiResponse<?> signUpWithKakao(@RequestParam String code) {
        log.info("[Kakao Signup] code {}", code);
        return ApiResponse.ok(userService.signUpWithKakao(code));
    }

    @GetMapping("/naver")
    public ApiResponse<?> signUpWithNaver(@RequestParam String code, @RequestParam String state) {
        log.info("[Kakao Signup] code {}, statue {}", code, state);
        return ApiResponse.ok(userService.signUpWithNaver(code));
    }
}