package by.kazachenko.ejka.additive.mapper;

import by.kazachenko.ejka.additive.dto.request.OriginRequest;
import by.kazachenko.ejka.additive.dto.response.OriginResponse;
import by.kazachenko.ejka.additive.model.Origin;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;

@Mapper(
        componentModel = ComponentModel.SPRING,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface OriginMapper {

    OriginResponse toResponse(Origin origin);

    @Mapping(target = "type", expression = "java(request.type().toUpperCase())")
    Origin toEntity(OriginRequest request);

}
