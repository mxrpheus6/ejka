package by.kazachenko.ejka.product.service.impl;

import by.kazachenko.ejka.common.exception.ExceptionMessages;
import by.kazachenko.ejka.common.exception.cutom.UserNotFoundException;
import by.kazachenko.ejka.common.security.SecurityUtils;
import by.kazachenko.ejka.common.utils.RedisKeyUtils;
import by.kazachenko.ejka.user.model.User;
import by.kazachenko.ejka.user.repository.UserRepository;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ScanPermissionService {

    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;
    private final StringRedisTemplate redisTemplate;

    @Value("${app.limits.free-scans}")
    private int maxFreeScans;

    @Transactional(readOnly = true)
    public void checkAndRecordScan() {
        UUID userId = securityUtils.getLoggedUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(ExceptionMessages.USER_NOT_FOUND));

        boolean isPremiumActive = user.getIsPremium() != null && user.getIsPremium() &&
                (user.getPremiumUntil() == null || !LocalDate.now()
                        .isAfter(user.getPremiumUntil()));

        if (isPremiumActive) {
            return;
        }

        String redisKey = RedisKeyUtils.getUserScansKey(userId);

        String currentScansStr = redisTemplate.opsForValue().get(redisKey);
        int currentScans = currentScansStr != null ? Integer.parseInt(currentScansStr) : 0;

        if (currentScans >= maxFreeScans) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Лимит бесплатных сканирований исчерпан. Оформите Premium."
            );
        }

        Long newScansCount = redisTemplate.opsForValue().increment(redisKey);

        if (newScansCount != null && newScansCount == 1) {
            LocalDateTime midnight = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
            Duration timeUntilMidnight = Duration.between(LocalDateTime.now(), midnight);
            redisTemplate.expire(redisKey, timeUntilMidnight);
        }
    }
}