package com.daramg.server.domain.user.presentation;

import com.daramg.server.domain.user.application.UserService;
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
    @ResponseStatus(HttpStatus.OK)
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
}
