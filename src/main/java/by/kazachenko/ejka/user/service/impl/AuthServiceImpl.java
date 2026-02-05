package by.kazachenko.ejka.user.service.impl;

import by.kazachenko.ejka.common.exception.cutom.UserAlreadyExistsException;
import by.kazachenko.ejka.common.security.CustomUserDetails;
import by.kazachenko.ejka.user.dto.response.AuthResponse;
import by.kazachenko.ejka.user.dto.request.LoginRequest;
import by.kazachenko.ejka.user.dto.request.RegisterRequest;
import by.kazachenko.ejka.user.model.User;
import by.kazachenko.ejka.user.model.enums.Role;
import by.kazachenko.ejka.user.repository.UserRepository;
import by.kazachenko.ejka.user.service.AuthService;
import by.kazachenko.ejka.user.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException("Пользователь с таким email уже существует");
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.ROLE_USER)
                .build();

        userRepository.save(user);


        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return new AuthResponse(accessToken, refreshToken);
    }

    public AuthResponse login(LoginRequest request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = customUserDetails.getUser();

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return new AuthResponse(accessToken, refreshToken);
    }

}
