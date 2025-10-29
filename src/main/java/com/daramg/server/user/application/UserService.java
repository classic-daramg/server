package com.daramg.server.user.application;

import com.daramg.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public boolean isNicknameAvailable(String nickName) {
        return !userRepository.existsByNickname(nickName);
    }
}
