package by.kazachenko.ejka.user.service.impl;

import by.kazachenko.ejka.common.exception.ExceptionMessages;
import by.kazachenko.ejka.common.exception.cutom.UserNotFoundException;
import by.kazachenko.ejka.common.security.SecurityUtils;
import by.kazachenko.ejka.user.dto.request.UserRequest;
import by.kazachenko.ejka.user.dto.response.UserResponse;
import by.kazachenko.ejka.user.mapper.UserMapper;
import by.kazachenko.ejka.user.model.User;
import by.kazachenko.ejka.user.repository.UserRepository;
import by.kazachenko.ejka.user.service.UserService;

import java.util.UUID;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    private final SecurityUtils securityUtils;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserProfile() {
        UUID loggedUserId = securityUtils.getLoggedUserId();

        User user = userRepository.findById(loggedUserId)
                .orElseThrow(() -> new UserNotFoundException(ExceptionMessages.USER_NOT_FOUND));

        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUser(UUID userId, UserRequest request) {
        return null;
    }

    @Override
    @Transactional
    public void deleteUserProfile() {
        UUID loggedUserId = securityUtils.getLoggedUserId();

        User user = userRepository.findById(loggedUserId)
                .orElseThrow(() -> new UserNotFoundException(ExceptionMessages.USER_NOT_FOUND));

        userRepository.delete(user);
    }
}
