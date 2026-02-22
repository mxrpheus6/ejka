package by.kazachenko.ejka.product.mapper;

import by.kazachenko.ejka.product.dto.request.ProductRequest;
import by.kazachenko.ejka.product.dto.response.ProductResponse;
import by.kazachenko.ejka.product.model.Product;
import org.mapstruct.BeanMapping;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants.ComponentModel;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = ComponentModel.SPRING,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface ProductMapper {

    ProductResponse toResponse(Product product);
    Product toEntity(ProductRequest productRequest);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateProductFromDto(ProductRequest productRequest, @MappingTarget Product product);

}
