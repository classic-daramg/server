package com.daramg.server.auth.application;

import com.daramg.server.common.exception.BusinessException;
import com.daramg.server.domain.user.domain.User;
import com.daramg.server.auth.domain.SignupVo;
import com.daramg.server.auth.dto.LoginDto;
import com.daramg.server.auth.dto.PasswordDto;
import com.daramg.server.auth.dto.SignupDto;
import com.daramg.server.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    public void signup(SignupDto dto){
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new BusinessException("중복된 이메일입니다.");
        }
        if (userRepository.existsByNickname(dto.getNickname())){
            throw new BusinessException("중복된 닉네임입니다.");
        }
        // TODO: bio, 닉네임에 금칙어 검사
        SignupVo vo = new SignupVo(
                dto.getName(),
                dto.getBirthdate(),
                dto.getEmail(),
                dto.getPassword(),
                dto.getProfileImage(),
                dto.getNickname(),
                dto.getBio()
        );
        User user = User.from(vo);
        userRepository.save(user);
    }

    public void login(LoginDto dto){
    }

    public void resetPassword(PasswordDto dto, User user){
        user.changePassword(dto.getPassword());
    }
}
