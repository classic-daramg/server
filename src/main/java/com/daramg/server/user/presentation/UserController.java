package com.daramg.server.user.presentation;

import com.daramg.server.user.application.UserService;
import com.daramg.server.user.domain.User;
import com.daramg.server.user.dto.UserProfileResponseDto;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    @GetMapping("/check-nickname")
    public ResponseEntity<Map<String, Boolean>> checkNicknameDuplication(
            @RequestParam
            @Pattern(
                    regexp = "^[a-zA-Z0-9가-힣._]{2,8}$",
                    message = "닉네임은 2~8자의 한글, 영문, 숫자와 일부 특수문자(_, .)만 사용할 수 있습니다."
            )
            String nickname) {
        boolean isAvailable = userService.isNicknameAvailable(nickname);
        return ResponseEntity.ok(Map.of("닉네임 사용 가능 유무: ", isAvailable));
    }

    @GetMapping
    public ResponseEntity<UserProfileResponseDto> getProfile(User user) {
        UserProfileResponseDto response = userService.getProfile(user);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/verify-user-email")
    public ResponseEntity<Map<String, Boolean>> verifyUserEmail(
            @RequestParam String email, User user) {
        boolean isValid = userService.verifyUserEmail(user, email);
        return ResponseEntity.ok(Map.of("유저 이메일 일치 여부 ", isValid));
    }

    @PostMapping("/following/{followedId}")
    @ResponseStatus(HttpStatus.OK)
    public void follow(@PathVariable Long followedId, User follower) {
        userService.follow(follower, followedId);
    }

    @DeleteMapping("/unfollowing/{followedId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unfollow(@PathVariable Long followedId, User follower) {
        userService.unfollow(follower, followedId);
    }
}
