package by.kazachenko.ejka.user.service.impl;

import by.kazachenko.ejka.common.exception.ExceptionMessages;
import by.kazachenko.ejka.common.exception.cutom.UserNotFoundException;
import by.kazachenko.ejka.common.security.SecurityUtils;
import by.kazachenko.ejka.common.service.impl.MinioServiceImpl;
import by.kazachenko.ejka.user.dto.request.UserRequest;
import by.kazachenko.ejka.user.dto.response.UserResponse;
import by.kazachenko.ejka.user.mapper.UserMapper;
import by.kazachenko.ejka.user.model.User;
import by.kazachenko.ejka.user.repository.UserRepository;
import by.kazachenko.ejka.user.service.UserService;

import java.util.UUID;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    private final SecurityUtils securityUtils;
    private final MinioServiceImpl minioService;

    @Value("${minio.buckets.avatars}")
    private String avatarsBucketName;

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

        String avatarKey = user.getAvatarKey();

        userRepository.delete(user);

        if (avatarKey != null && !avatarKey.isBlank()) {
            minioService.deleteFile(avatarsBucketName, avatarKey);
        }
    }

    @Override
    @Transactional
    public void uploadAvatar(MultipartFile file) {
        UUID loggedUserId = securityUtils.getLoggedUserId();
        User user = userRepository.findById(loggedUserId)
                .orElseThrow(() -> new UserNotFoundException(ExceptionMessages.USER_NOT_FOUND));

        String newObjectKey = minioService.uploadFile(file, avatarsBucketName);

        String oldObjectKey = user.getAvatarKey();

        user.setAvatarKey(newObjectKey);
        userRepository.save(user);

        if (oldObjectKey != null && !oldObjectKey.isBlank()) {
            minioService.deleteFile(avatarsBucketName, oldObjectKey);
        }
    }

    @Override
    @Transactional
    public void deleteAvatar() {
        UUID loggedUserId = securityUtils.getLoggedUserId();
        User user = userRepository.findById(loggedUserId)
                .orElseThrow(() -> new UserNotFoundException(ExceptionMessages.USER_NOT_FOUND));

        String avatarKey = user.getAvatarKey();

        if (avatarKey != null && !avatarKey.isBlank()) {
            minioService.deleteFile(avatarsBucketName, avatarKey);
            user.setAvatarKey(null);
            userRepository.save(user);
        }
    }
}
