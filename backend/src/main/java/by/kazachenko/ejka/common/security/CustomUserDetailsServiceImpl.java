package by.kazachenko.ejka.common.security;

import by.kazachenko.ejka.user.model.User;
import by.kazachenko.ejka.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return CustomUserDetails.builder()
                .id(user.getId().toString())
                .email(user.getEmail())
                .password(user.getPassword())
                .role(user.getRole())
                .tokenVersion(user.getTokenVersion())
                .build();
    }
}
