package cz.demo.usermanagement.mapper;



import cz.demo.usermanagement.controller.dto.GetUserResponse;
import cz.demo.usermanagement.controller.dto.SaveUserRequest;
import cz.demo.usermanagement.repository.entity.UserEntity;
import cz.demo.usermanagement.service.domain.User;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;

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
    User toUser(Long id, SaveUserRequest saveUserRequest);

    /**
     * Mapping method: User -> GetUserResponseData
     *
     * @return GetUserResponseData
     */
    GetUserResponse userToGetUserResponseData(User user);


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
}
