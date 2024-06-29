package cz.demo.usermanagement.mapper;


import cz.demo.usermanagement.repository.entity.UserEntity;
import cz.demo.usermanagement.service.domain.User;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", //managed by Spring as a singleton
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
)
public interface UserMapper {

    /**
     * Mapovaci metoda: User -> UserEntity
     *
     * @return UserEntity
     */
    UserEntity toUserEntity(User user);

    /**
     * Mapovaci metoda: UserEntity -> User
     *
     * @return User
     */
    User toUser(UserEntity user);
}
