package cz.demo.usermanagement.repository;

import cz.demo.usermanagement.repository.entity.User;
import cz.demo.usermanagement.repository.enums.ROLE;

import java.util.List;
import java.util.Optional;

/**
 * DAO repository for {@link User} for more granular approach than UserRepository
 */
public interface UserDAO {

    User save(User user);

    User update(User user);

    void deleteById(int id);

    Optional<User> findById(int id);

    List<User> findAll();

    Optional<User> findByUserName(String userName);

    Optional<User> findByUserName(String userName, boolean fetchRoles);

    List<User> findByRole(ROLE role);

}
