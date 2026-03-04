package by.kazachenko.ejka.additive.dto.response;

import by.kazachenko.ejka.additive.model.enums.DangerLevel;
import java.util.Set;

public record AdditiveResponse(
        Long id,
        String code,
        String nameRu,
        String nameEn,
        String category,
        DangerLevel dangerLevel,
        String warningDescription,
        String description,
        Set<OriginResponse> origins
) {

}
