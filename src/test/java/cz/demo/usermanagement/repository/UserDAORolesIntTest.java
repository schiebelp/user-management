package cz.demo.usermanagement.repository;

import cz.demo.usermanagement.repository.entity.User;
import cz.demo.usermanagement.repository.entity.Role;
import cz.demo.usermanagement.repository.enums.ROLE;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class UserDAORolesIntTest {

    @Autowired
    private UserDAO userDAO;

    private User user1;
    private User user2;
    private Role adminRole;
    private Role userRole;

    @BeforeEach
    void setUp() {
        // Create roles
        adminRole = Role.builder().name(ROLE.ROLE_ADMIN).build();
        userRole = Role.builder().name(ROLE.ROLE_USER).build();


        // Create users with roles
        user1 = User.builder()
                .firstName("John")
                .lastName("Doe")
                .userName("johndoe")
                .password("password123")
                .roles(new HashSet<>(Collections.singletonList(adminRole)))
                .build();

        user2 = User.builder()
                .firstName("Jane")
                .lastName("Smith")
                .userName("janesmith")
                .password("password456")
                .roles(new HashSet<>(Collections.singletonList(userRole)))
                .build();

        // Save users (assuming your DAO save method handles cascading role saves)
        user1 = userDAO.save(user1);
        user2 = userDAO.save(user2);
    }

    @Nested
    @DisplayName("User Role Assignment")
    class UserRoleAssignment {

        @Test
        @DisplayName("User has assigned role successfully")
        void whenUserHasAssignedRole_thenSuccess() {
            // when
            User retrievedUser1 = userDAO.findById(user1.getId()).orElseThrow();
            User retrievedUser2 = userDAO.findById(user2.getId()).orElseThrow();

            // then
            assertThat(retrievedUser1.getRoles()).extracting("name").containsExactly(ROLE.ROLE_ADMIN);
            assertThat(retrievedUser2.getRoles()).extracting("name").containsExactly(ROLE.ROLE_USER);
        }

        @Test
        @DisplayName("Assign additional role to user successfully")
        void whenAssignAdditionalRoleToUser_thenSuccess() {
            // given
            user2.getRoles().add(adminRole);

            // when
            userDAO.update(user2);

            // then
            User updatedUser = userDAO.findById(user2.getId()).orElseThrow();
            assertThat(updatedUser.getRoles()).extracting("name").containsExactlyInAnyOrder(ROLE.ROLE_USER, ROLE.ROLE_ADMIN);
        }

        @Test
        @DisplayName("Remove role from user successfully")
        void whenRemoveRoleFromUser_thenSuccess() {
            // given
            user1.setRoles(Collections.emptySet());

            // when
            userDAO.update(user1);

            // then
            User updatedUser = userDAO.findById(user1.getId()).orElseThrow();
            assertThat(updatedUser.getRoles()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Find Users by Role")
    class FindUsersByRole {

        @Test
        @DisplayName("Find users by admin role successfully")
        void whenFindUsersByAdminRole_thenSuccess() {
            // when
            List<User> adminUsers = userDAO.findByRole(ROLE.ROLE_ADMIN);

            // then
            assertThat(adminUsers).extracting("userName").containsExactly("johndoe");
        }

        @Test
        @DisplayName("Find users by user role successfully")
        void whenFindUsersByUserRole_thenSuccess() {
            // when
            List<User> regularUsers = userDAO.findByRole(ROLE.ROLE_USER);

            // then
            assertThat(regularUsers).extracting("userName").containsExactly("janesmith");
        }
    }
}