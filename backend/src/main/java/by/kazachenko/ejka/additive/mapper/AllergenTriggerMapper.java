package by.kazachenko.ejka.additive.mapper;

import by.kazachenko.ejka.additive.dto.response.AllergenTriggerResponse;
import by.kazachenko.ejka.additive.model.AllergenTrigger;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants.ComponentModel;

@Mapper(
        componentModel = ComponentModel.SPRING,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface AllergenTriggerMapper {

    AllergenTriggerResponse toResponse(AllergenTrigger allergenTrigger);

}
