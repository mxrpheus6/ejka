package by.kazachenko.ejka.review.mapper;

import by.kazachenko.ejka.review.dto.request.ReviewRequest;
import by.kazachenko.ejka.review.dto.response.ReviewResponse;
import by.kazachenko.ejka.review.model.Review;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;

@Mapper(
        componentModel = ComponentModel.SPRING,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface ReviewMapper {

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "authorId", source = "author.id")
    @Mapping(target = "username", source = "author.username")
    ReviewResponse toResponse(Review review);

    @Mapping(target = "product", ignore = true)
    @Mapping(target = "author", ignore = true)
    Review toEntity(ReviewRequest reviewRequest);

}
