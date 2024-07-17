package cz.demo.usermanagement.repository;

import cz.demo.usermanagement.repository.entity.User;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace= Replace.NONE)
@ActiveProfiles("test")
@Tag("integration-test")
@DisplayName("Given user repository with 2 users")
class UserDAOIntTest {

    @Autowired
    private UserDAO tested;

    private User existingUser1;
    private User existingUser2;

    @BeforeEach
    public void setUp() {
        existingUser1 = User.builder()
                .firstName("John")
                .lastName("Doe")
                .userName("johndoe")
                .password("password123")
                .build();

        existingUser2 = User.builder()
                .firstName("Jane")
                .lastName("Smith")
                .userName("janesmith")
                .password("password456")
                .build();

        tested.save(existingUser1);
        tested.save(existingUser2);

    }

    @AfterEach
    void tearDown() {
        tested.deleteById(existingUser1.getId());
        tested.deleteById(existingUser2.getId());
    }

    @Nested
    @DisplayName("Save user")
    class Save{

        @Test
        @DisplayName("new success")
        void whenNewUser_thenSave_success() {

            // given
            User user = User.builder()
                    .firstName("Bilbo")
                    .lastName("Baggins")
                    .userName("bilbobagins")
                    .password("passwordbilbo123")
                    .build();

            // when
            User savedUser = tested.save(user);

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
        @DisplayName("new password success")
        void whenNewPassword_thenSave_success() {

            // given
            var newPassword = "password123";
            existingUser1.setPassword(newPassword);

            // when
            User savedUser = tested.save(existingUser1);

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
            Optional<User> foundUser = tested.findById(existingUser1.getId());

            // then
            assertThat(foundUser).isPresent();

        }

        @Test
        @DisplayName("non existing user fails")
        void whenNonExistingUser_thenFindById_fail() {

            // when
            Optional<User> foundUser = tested.findById(99);

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
            List<User> users = tested.findAll();

            // then
            assertThat(users).hasSize(2);
            assertThat(users)
                    .extracting(User::getUserName)
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
            Optional<User> foundUser = tested.findByUserName(existingUser1.getUserName());

            // then
            assertThat(foundUser).isPresent();

        }

        @Test
        @DisplayName("does not find non existent user")
        void whenNonExistingUser_thenFindByUserName_fail() {

            // when
            Optional<User> foundUser = tested.findByUserName("nonexistinguser");

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
            Optional<User> foundUser = tested.findById(id);
            assertThat(foundUser).isNotPresent();
        }

        @Test
        @DisplayName("non existing user silently fails")
        void whenNonExistingUser_thenDeleteById_silentFail() {

            assertDoesNotThrow(() -> tested.deleteById(999));

        }

    }
}
