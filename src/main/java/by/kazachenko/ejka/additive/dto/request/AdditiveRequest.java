package by.kazachenko.ejka.additive.dto.request;

import by.kazachenko.ejka.additive.model.enums.DangerLevel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record AdditiveRequest(

        @NotBlank(message = "{additive_request.code.not_blank}")
        @Size(max = 30, message = "{additive_request.code.size}")
        String code,

        @Size(max = 100, message = "{additive_request.nameRu.size}")
        String nameRu,

        @Size(max = 100, message = "{additive_request.nameEn.size}")
        String nameEn,

        @Size(max = 100, message = "{additive_request.category.size}")
        String category,

        DangerLevel dangerLevel,

        String warningDescription,

        String description,

        Set<Long> originIds

) {

}
