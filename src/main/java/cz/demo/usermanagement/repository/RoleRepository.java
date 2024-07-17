package cz.demo.usermanagement.repository;

import cz.demo.usermanagement.repository.entity.Role;
import cz.demo.usermanagement.repository.enums.ROLE;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


/**
 * CRUD repository for Role
 */
@Repository
public interface RoleRepository extends CrudRepository<Role, Integer> {
    Optional<Role> findByName(ROLE name);
}
