package by.kazachenko.ejka.user.service.impl;

import by.kazachenko.ejka.common.exception.ExceptionMessages;
import by.kazachenko.ejka.common.exception.cutom.UserAlreadyExistsException;
import by.kazachenko.ejka.common.security.CustomUserDetails;
import by.kazachenko.ejka.user.dto.request.RefreshTokenRequest;
import by.kazachenko.ejka.user.dto.response.AuthResponse;
import by.kazachenko.ejka.user.dto.request.LoginRequest;
import by.kazachenko.ejka.user.dto.request.RegisterRequest;
import by.kazachenko.ejka.user.model.User;
import by.kazachenko.ejka.user.model.enums.Role;
import by.kazachenko.ejka.user.repository.UserRepository;
import by.kazachenko.ejka.user.service.AuthService;
import by.kazachenko.ejka.user.service.JwtService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException(ExceptionMessages.USER_EMAIL_ALREADY_EXISTS);
        }

        if (userRepository.existsByUsername(request.username())) {
            throw new UserAlreadyExistsException(ExceptionMessages.USER_USERNAME_ALREADY_EXISTS);
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .username(request.username())
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

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        return new AuthResponse(accessToken, refreshToken);
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        Claims claims = jwtService.extractAllClaims(refreshTokenRequest.refreshToken());
        String email = claims.getSubject();
        Integer tokenVersion = claims.get("version", Integer.class);

        User user = userRepository.findByEmail(email)
                .orElseThrow();

        if (!user.getTokenVersion().equals(tokenVersion)) {
            throw new BadCredentialsException(ExceptionMessages.TOKEN_INVALID);
        }

        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        return new AuthResponse(newAccessToken, newRefreshToken);
    }

    @Override
    @Transactional
    public void invalidateAllSessions(String email) {
        userRepository.incrementTokenVersion(email);
    }

}
