package by.kazachenko.ejka.common.utils;

import java.util.UUID;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RedisKeyUtils {

    private static final String SCANS_PREFIX = "scans:user:";

    public static String getUserScansKey(UUID userId) {
        return SCANS_PREFIX + userId.toString();
    }
}
