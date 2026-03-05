package by.kazachenko.ejka.product.dto.response;

import by.kazachenko.ejka.product.model.enums.ProductImageType;

public record ProductImageResponse(

        ProductImageType type,

        String objectKey

) {

}
