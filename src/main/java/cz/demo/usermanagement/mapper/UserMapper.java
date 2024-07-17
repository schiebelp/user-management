package cz.demo.usermanagement.mapper;


import cz.demo.usermanagement.controller.dto.PartialyUpdateUserRequest;
import cz.demo.usermanagement.controller.dto.SaveUserRequest;
import cz.demo.usermanagement.controller.dto.UserResponse;
import cz.demo.usermanagement.repository.entity.Role;
import cz.demo.usermanagement.repository.entity.User;
import cz.demo.usermanagement.repository.enums.ROLE;
import org.mapstruct.*;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

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
    @Mapping(target = "roles", source = "saveUserRequest.roles", qualifiedByName = "stringSetToRoleSet")
    User toUser(Integer id, SaveUserRequest saveUserRequest);

    /**
     * Mapping method: SaveUserRequest -> User
     *
     * @return User
     */
    @Mapping(target = "roles", source = "saveUserRequest.roles", qualifiedByName = "stringSetToRoleSet")
    User toUser(Integer id, PartialyUpdateUserRequest saveUserRequest);

    /**
     * Mapping method: User -> UserResponse
     *
     * @return UserResponse
     */
    @Mapping(target = "roles", source = "roles", qualifiedByName = "roleSetToStringSet")
    UserResponse toUserResponse(User user);

    /**
     * Convert a list of role names to a set of Role entities
     */
    @Named("stringSetToRoleSet")
    default Set<Role> stringSetToRoleSet(Set<String> roleNames) {
        if (roleNames == null) {
            return Collections.emptySet();
        }
        return roleNames.stream()
                .map(this::createRole)
                .collect(Collectors.toSet());
    }

    /**
     * Convert a list of Role entities to a set of role names
     */
    @Named("roleSetToStringSet")
    default Set<String> roleSetToStringSet(Set<Role> roles) {
        if (CollectionUtils.isEmpty(roles)) {
            return Collections.emptySet();
        }
        return roles.stream()
                .map(this::createRole)
                .collect(Collectors.toSet());
    }

    /**
     * Create a Role entity from a role name
     */
    default Role createRole(String roleName) {
        return Role.builder().name(ROLE.valueOf(roleName)).build();
    }

    /**
     * Create a String from a Role name
     */
    default String createRole(Role role) {
        return role.getName().name();
    }

    /**
     * Update Entity from user
     *
     * @param to original user
     * @param from updated user
     */
    @Mapping(target = "id", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void update(User from, @MappingTarget User to);

}
