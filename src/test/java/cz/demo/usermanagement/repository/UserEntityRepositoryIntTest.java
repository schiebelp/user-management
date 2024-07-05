package cz.demo.usermanagement.repository;

import cz.demo.usermanagement.AbstractIntegrationTest;
import cz.demo.usermanagement.model.UserEntity;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace= Replace.NONE)
@DisplayName("Given user repository with 2 users")
class UserEntityRepositoryIntTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository tested;

    private UserEntity existingUser1;
    private UserEntity existingUser2;
    private List<UserEntity> existingUsers;

    @BeforeEach
    public void setUp() {

        // Create 2 users
        // 1. John Doe
        // 2. Jane Smith

        existingUser1 = createUserEntiy(null, "John", "Doe", "johndoe", "password123");
        existingUser2 = createUserEntiy(null, "Jane", "Smith", "janesmith", "password456");

        existingUsers = Arrays.asList(existingUser1, existingUser2);

        tested.saveAll(existingUsers);
    }

    @AfterEach
    public void cleanUp() {
        // Delete all existing users
        tested.deleteAll(existingUsers);

        // Assert that all users were deleted
        List<UserEntity> users = tested.findAllById(
                existingUsers.stream()
                        .map(UserEntity::getId)
                        .toList() );

        assertThat(users).isEmpty();
    }

    @Nested
    @DisplayName("Save user")
    class Save{

        @Test
        @DisplayName("new means success")
        void whenNewUser_thenSave_success() {

            // given
            UserEntity user = UserEntity.builder()
                    .firstName("Bilbo")
                    .lastName("Baggins")
                    .userName("bilbobagins")
                    .password("passwordbilbo123")
                    .build();

            // when
            UserEntity savedUser = tested.save(user);

            // then
            assertAll("Saved User",
                    () -> assertThat(savedUser.getId()).isPositive(),
                    () -> assertThat(savedUser.getFirstName()).isEqualTo(user.getFirstName()),
                    () -> assertThat(savedUser.getLastName()).isEqualTo(user.getLastName()),
                    () -> assertThat(savedUser.getUserName()).isEqualTo(user.getUserName()),
                    () -> assertThat(savedUser.getPassword()).isEqualTo(user.getPassword())
            );

        }

        @Test
        @DisplayName("new password means success")
        void whenNewPassword_thenSave_success() {

            // given
            var newPassword = "password123";
            existingUser1.setPassword(newPassword);

            // when
            UserEntity savedUser = tested.save(existingUser1);

            // then
            assertThat(savedUser.getPassword()).isEqualTo(newPassword);
        }

        @Test
        @DisplayName("null fails")
        void whenNullUser_thenSave_fail() {

            assertThrows(RuntimeException.class, () -> tested.save(null));

        }

    }

    @Nested
    @DisplayName("Find by id")
    class FindById{

        @Test
        @DisplayName("existing user success")
        void whenExistingUser_thenFindById_success() {

            // when
            Optional<UserEntity> foundUser = tested.findById(existingUser1.getId());

            // then
            assertThat(foundUser).isPresent();

        }

        @Test
        @DisplayName("non existing user fails")
        void whenNonExistingUser_thenFindById_fail() {

            // when
            Optional<UserEntity> foundUser = tested.findById(99);

            // then
            assertThat(foundUser).isNotPresent();

        }

    }

    @Nested
    @DisplayName("Find all")
    class FindAll{

        @Test
        @DisplayName("finds all users success")
        void whenExistingUsers_thenFindAll_success() {

            // when
            List<UserEntity> users = tested.findAll();

            // then
            assertThat(users).hasSize(2);
            assertThat(users)
                    .extracting(UserEntity::getUserName)
                    .containsExactlyInAnyOrder(existingUser1.getUserName(), existingUser2.getUserName() );

        }

    }

    @Nested
    @DisplayName("Find by username")
    class FindByUserName{

        @Test
        @DisplayName("finds existing user")
        void whenExistingUser_thenFindByUserName_success() {

            // when
            Optional<UserEntity> foundUser = tested.findByUserName(existingUser1.getUserName());

            // then
            assertThat(foundUser).isPresent();

        }

        @Test
        @DisplayName("does not find non existent user")
        void whenNonExistingUser_thenFindByUserName_fail() {

            // when
            Optional<UserEntity> foundUser = tested.findByUserName("nonexistinguser");

            // then
            assertThat(foundUser).isNotPresent();

        }

    }

    @Nested
    @DisplayName("Delete By Id")
    class DeleteById{

        @Test
        @DisplayName("existing user success")
        void whenExistingUser_thenDeleteById_success() {

            var id= existingUser1.getId();

            // when
            tested.deleteById(id);

            // then
            Optional<UserEntity> foundUser = tested.findById(id);
            assertThat(foundUser).isNotPresent();
        }

        @Test
        @DisplayName("non existing user silently fails")
        void whenNonExistingUser_thenDeleteById_silentFail() {

            assertDoesNotThrow(() -> tested.deleteById(999));

        }

    }

}
