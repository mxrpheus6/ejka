package by.kazachenko.ejka.additive.mapper;

import by.kazachenko.ejka.additive.dto.request.AdditiveRequest;
import by.kazachenko.ejka.additive.dto.response.AdditiveResponse;
import by.kazachenko.ejka.additive.dto.response.WorkerAdditiveResponse;
import by.kazachenko.ejka.additive.model.Additive;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;
import org.mapstruct.MappingTarget;

@Mapper(
        componentModel = ComponentModel.SPRING,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface AdditiveMapper {

    AdditiveResponse toResponse(Additive additive);

    WorkerAdditiveResponse toWorkerResponse(Additive additive);

    @Mapping(target = "id", ignore = true)
    Additive toEntity(AdditiveRequest additiveRequest);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromRequest(AdditiveRequest request, @MappingTarget Additive additive);
}