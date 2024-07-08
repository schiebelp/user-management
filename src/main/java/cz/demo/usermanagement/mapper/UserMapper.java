package cz.demo.usermanagement.mapper;



import cz.demo.usermanagement.controller.dto.UpdateUserRequest;
import cz.demo.usermanagement.controller.dto.UserResponse;
import cz.demo.usermanagement.controller.dto.SaveUserRequest;
import cz.demo.usermanagement.repository.entity.UserEntity;
import cz.demo.usermanagement.service.domain.User;
import org.mapstruct.*;

/**
 * Mapstruct for conversion between Controller - Service - Repository
 */
@Mapper(componentModel = "spring", //managed by Spring as a singleton
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
)
public interface UserMapper {

    /**
     * Mapping method: SaveUserRequest -> User
     *
     * @return User
     */
    User toUser(Integer id, SaveUserRequest saveUserRequest);

    /**
     * Mapping method: SaveUserRequest -> User
     *
     * @return User
     */
    User toUser(Integer id, UpdateUserRequest saveUserRequest);

    /**
     * Mapping method: User -> GetUserResponseData
     *
     * @return GetUserResponseData
     */
    UserResponse toUserResponse(User user);


    /**
     * Mapping method: User -> UserEntity
     *
     * @return UserEntity
     */
    UserEntity toUserEntity(User user);

    /**
     * Mapping method: UserEntity -> User
     *
     * @return User
     */
    User toUser(UserEntity user);

    /**
     * Update Entity from user
     * @param user from
     * @param userEntity to
     */
    @Mapping(target = "id", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(User user, @MappingTarget UserEntity userEntity);
}
