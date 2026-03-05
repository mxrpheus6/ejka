package by.kazachenko.ejka.additive.dto.request;

import jakarta.validation.constraints.NotBlank;

public record OriginRequest(

        @NotBlank(message = "{origin_request.type.not_blank}")
        String type

) {

}
