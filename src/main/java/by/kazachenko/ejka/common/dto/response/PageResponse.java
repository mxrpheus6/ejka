package by.kazachenko.ejka.common.dto.response;

import java.util.List;
import lombok.Builder;

@Builder
public record PageResponse<T>(
        int currentOffset,
        int currentLimit,
        int totalPages,
        long totalElements,
        String sort,
        List<T> values
) {
}
