package by.kazachenko.ejka.additive.mapper;

import by.kazachenko.ejka.additive.dto.request.AdditiveRequest;
import by.kazachenko.ejka.additive.dto.response.AdditiveResponse;
import by.kazachenko.ejka.additive.dto.response.OriginResponse;
import by.kazachenko.ejka.additive.dto.response.WorkerAdditiveResponse;
import by.kazachenko.ejka.additive.model.Additive;
import by.kazachenko.ejka.additive.model.Origin;

import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(
        componentModel = ComponentModel.SPRING,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface AdditiveMapper {

    @Mapping(target = "origins", source = "origins", qualifiedByName = "mapOrigins")
    AdditiveResponse toResponse(Additive additive);

    WorkerAdditiveResponse toWorkerResponse(Additive additive);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "origins", ignore = true)
    Additive toEntity(AdditiveRequest additiveRequest);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "origins", ignore = true)
    void updateEntityFromRequest(AdditiveRequest request, @MappingTarget Additive additive);

    @Named("mapOrigins")
    default Set<OriginResponse> mapOrigins(Set<Origin> origins) {
        return origins.stream()
                .map(o -> new OriginResponse(o.getId(), o.getType()))
                .collect(Collectors.toSet());
    }

}
