package by.kazachenko.ejka.additive.dto.response;

import by.kazachenko.ejka.additive.model.enums.AllergenCategory;

public record AllergenTriggerResponse(
        Long id,
        AllergenCategory category,
        String triggerWord
) {

}
