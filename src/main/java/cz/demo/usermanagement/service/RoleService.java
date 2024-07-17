package cz.demo.usermanagement.service;

import cz.demo.usermanagement.repository.RoleRepository;
import cz.demo.usermanagement.repository.entity.Role;
import cz.demo.usermanagement.repository.enums.ROLE;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Business logic for Roles
 *
 * Currently only helper methods
 */
@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    @Transactional // note: default is REQUIRED - inside the same transaction or brand new
    public Role createRoleIfNotExists(ROLE roleName) {
        return roleRepository.findByName(roleName)
                .orElseGet(() -> roleRepository.save(Role.builder().name(roleName).build()));
    }

}
