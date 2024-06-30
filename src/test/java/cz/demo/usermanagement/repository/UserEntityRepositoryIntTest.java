package cz.demo.usermanagement.repository;

import cz.demo.usermanagement.repository.entity.UserEntity;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@AutoConfigureTestDatabase(replace= Replace.NONE)
@ActiveProfiles("test")
@Tag("integration-test")
@DisplayName("Given user repository with 2 users")
public class UserEntityRepositoryIntTest {

    @Autowired
    private UserRepository tested;

    private UserEntity existingUser1;
    private UserEntity existingUser2;
    private List<UserEntity> existingUsers;

    @BeforeEach
    public void setUp() {
        existingUser1 = UserEntity.builder()
                .firstName("John")
                .lastName("Doe")
                .userName("johndoe")
                .password("password123")
                .build();
        existingUser2 = UserEntity.builder()
                .firstName("Jane")
                .lastName("Smith")
                .userName("janesmith")
                .password("password456")
                .build();

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

    @Test
    @DisplayName("then find by id finds user")
    public void whenExistingUser_thenFindById_success() {

        // when
        Optional<UserEntity> foundUser = tested.findById(existingUser1.getId());

        // then
        assertThat(foundUser.isPresent()).isTrue();

    }

    @Test
    @DisplayName("then find by id wont find non existing user")
    public void whenNonExistingUser_thenFindById_fail() {

        // when
        Optional<UserEntity> foundUser = tested.findById(99L);

        // then
        assertThat(foundUser.isPresent()).isFalse();

    }

    @Test
    @DisplayName("then find by username finds user")
    public void whenExistingUser_thenFindByUserName_success() {

        // when
        Optional<UserEntity> foundUser = tested.findByUserName(existingUser1.getUserName());

        // then
        assertThat(foundUser.isPresent()).isTrue();

    }

    @Test
    @DisplayName("then find by id wont find non existing user")
    public void whenNonExistingUser_thenFindByUserName_fail() {

        // when
        Optional<UserEntity> foundUser = tested.findByUserName("nonexistinguser");

        // then
        assertThat(foundUser.isPresent()).isFalse();

    }

    @Test
    @DisplayName("then find all returns all 2 users")
    public void whenExistingUsers_thenFindAll_success() {

        // when
        List<UserEntity> users = tested.findAll();

        // then
        assertThat(users.size()).isEqualTo(2);
        assertThat(users)
                .extracting(UserEntity::getUserName)
                .containsExactlyInAnyOrder(existingUser1.getUserName(), existingUser2.getUserName() );

    }


    @Test
    @DisplayName("then save user creates new user")
    public void whenNewUser_thenSave_success() {

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
    @DisplayName("then saving invalid user throws exception")
    public void whenNullUser_thenSave_fail() {

        assertThrows(RuntimeException.class, () -> tested.save(null));

    }

    @Test
    @DisplayName("then save user updates user")
    public void whenNewPassword_thenSave_success() {

        // given
        var newPassword = "password123";
        existingUser1.setPassword(newPassword);

        // when
        UserEntity savedUser = tested.save(existingUser1);

        // then
        assertThat(savedUser.getPassword()).isEqualTo(newPassword);
    }

    @Test
    @DisplayName("then delete by id 1 deletes user 1")
    public void whenExistingUser_thenDeleteById_success() {

        var id= existingUser1.getId();

        // when
        tested.deleteById(id);

        // then
        Optional<UserEntity> foundUser = tested.findById(id);
        assertThat(foundUser.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("non existing user delete exception")
    public void whenNonExistingUser_thenDeleteById_throwsException() {

        assertThrows(RuntimeException.class, () -> tested.deleteById(999L));

    }


}
