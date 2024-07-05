package cz.demo.usermanagement.repository;

import cz.demo.usermanagement.model.UserEntity;

import java.util.List;

public interface UserDAO {

    void save(UserEntity user);

    void update(UserEntity user);

    void delete(int id);

    UserEntity findById(int id);

    List<UserEntity> findAll();

}
