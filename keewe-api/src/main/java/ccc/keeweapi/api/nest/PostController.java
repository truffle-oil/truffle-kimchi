package ccc.keeweapi.api.nest;

import ccc.keeweapi.dto.ApiResponse;
import ccc.keeweapi.dto.nest.AnnouncementCreateRequest;
import ccc.keeweapi.dto.nest.AnnouncementCreateResponse;
import ccc.keeweapi.dto.nest.PostResponse;
import ccc.keeweapi.dto.nest.VotePostCreateRequest;
import ccc.keeweapi.service.nest.PostApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/nest")
@RequiredArgsConstructor
public class PostController {

    private final PostApiService postApiService;


    @PostMapping("/announcement")
    public ApiResponse<AnnouncementCreateResponse> createAnnouncement(@Valid @RequestBody AnnouncementCreateRequest request) {
        return ApiResponse.ok(postApiService.createAnnouncementPost(request));
    }

    //TODO: 선택지 size, 선택지 내용 size, contents 145자 validate
    @PostMapping("/vote")
    public ApiResponse<PostResponse> createAnnouncementPost(@RequestBody VotePostCreateRequest request) {
        return ApiResponse.ok(postApiService.createPost(request));
    }
}