package by.kazachenko.ejka.service;

import by.kazachenko.ejka.dto.LoginRequest;
import by.kazachenko.ejka.dto.LoginResponse;
import by.kazachenko.ejka.dto.RegisterRequest;
import by.kazachenko.ejka.dto.RegisterResponse;
import by.kazachenko.ejka.model.User;
import by.kazachenko.ejka.model.enums.Role;
import by.kazachenko.ejka.repository.UserRepository;
import by.kazachenko.ejka.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public RegisterResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_USER)
                .build();

        userRepository.save(user);

        return new RegisterResponse("User registered successfully");
    }

    public LoginResponse login(LoginRequest request) {

        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        String accessToken = jwtService.generateAccessToken(request.getEmail());
        String refreshToken = jwtService.generateAccessToken(request.getEmail());

        return new LoginResponse(accessToken, refreshToken);
    }

}
