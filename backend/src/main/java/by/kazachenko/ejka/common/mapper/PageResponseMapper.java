package by.kazachenko.ejka.common.mapper;

import by.kazachenko.ejka.common.dto.response.PageResponse;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants.ComponentModel;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Mapper(
        componentModel = ComponentModel.SPRING,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface PageResponseMapper {

    default <T> PageResponse<T> toResponse(Page<T> page) {
        Pageable pageable = page.getPageable();

        return PageResponse.<T>builder()
                .currentOffset(pageable.getPageNumber())
                .currentLimit(pageable.getPageSize())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .sort(page.getSort().toString())
                .values(page.getContent())
                .build();
    }

}
