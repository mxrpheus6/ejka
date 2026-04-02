package by.kazachenko.ejka.user.mapper;

import by.kazachenko.ejka.user.dto.request.UserRequest;
import by.kazachenko.ejka.user.dto.response.UserResponse;
import by.kazachenko.ejka.user.model.User;

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
public interface UserMapper {

    UserResponse toResponse(User user);

    User toEntity(UserRequest userRequest);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUserFromDto(UserRequest userRequest, @MappingTarget User user);

}
