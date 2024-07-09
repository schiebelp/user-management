package cz.demo.usermanagement.repository;

import cz.demo.usermanagement.repository.entity.UserEntity;

import java.util.List;
import java.util.Optional;

/**
 * DAO repository for {@link UserEntity} for more granular approach than UserRepository
 */
public interface UserDAO {

    UserEntity save(UserEntity user);

    UserEntity update(UserEntity user);

    void deleteById(int id);

    Optional<UserEntity> findById(int id);

    List<UserEntity> findAll();

    Optional<UserEntity> findByUserName(String userName);

}
