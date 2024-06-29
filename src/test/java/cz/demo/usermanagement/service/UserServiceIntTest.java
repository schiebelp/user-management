package cz.demo.usermanagement.service;


import cz.demo.usermanagement.exception.UnauthorizedException;
import cz.demo.usermanagement.exception.UserNotFoundException;
import cz.demo.usermanagement.repository.UserRepository;
import cz.demo.usermanagement.repository.entity.UserEntity;
import cz.demo.usermanagement.service.domain.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Given user service with 2 users")
public class UserServiceIntTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserServiceImpl tested;

    private UserEntity existingUser1;
    private UserEntity existingUser2;

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

        userRepository.saveAll(Arrays.asList(existingUser1, existingUser2));
    }

    @AfterEach
    public void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("then get user by id finds user")
    public void whenUserExist_thenGetUserById_success() {
        // given
        Long userId = existingUser1.getId();

        // when
        User result = tested.getUserById(userId);

        // then
        assertAll("User by ID",
                () -> assertThat(result.getFirstName()).isEqualTo(existingUser1.getFirstName()),
                () -> assertThat(result.getLastName()).isEqualTo(existingUser1.getLastName()),
                () -> assertThat(result.getUserName()).isEqualTo(existingUser1.getUserName())
        );
    }

    @Test
    @DisplayName("non existing user throws exception")
    public void whenUserDontExist_thenGetUserById_throwsException() {

        assertThrows(UserNotFoundException.class, () -> tested.getUserById(0L));
    }

    @Test
    @DisplayName("then get all users returns all 2 users")
    public void whenUsersExist_thenGetAllUsers_finds() {
        // Act
        List<User> users = tested.getAllUsers();

        // Assert
        assertThat(users).isNotEmpty();
        assertThat(users).hasSize(2);

        assertThat(users)
                .extracting(User::getUserName)
                .containsExactlyInAnyOrder(existingUser1.getUserName(), existingUser2.getUserName() );
    }

    @Test
    @DisplayName("then create user saves new user")
    public void whenNewUser_thenCreateUser_success() {
        // given
        String firstName = "firstName";
        String lastName = "lastName";
        String userName = "userName";
        String password = "password";

        // when
        User user = tested.createUser(
                User.builder()
                        .firstName(firstName)
                        .lastName(lastName)
                        .userName(userName)
                        .password(password).build()
        );

        // then
        Optional<UserEntity> result = userRepository.findById(user.getId());
        assertThat(result).isPresent().get()
                .satisfies(r -> assertAll("Create User Result",
                        () -> assertThat(r.getFirstName()).isEqualTo(firstName),
                        () -> assertThat(r.getLastName()).isEqualTo(lastName),
                        () -> assertThat(r.getUserName()).isEqualTo(userName),
                        () -> assertThat(r.getPassword()).isEqualTo(password)
                ));

    }

    @Test
    @DisplayName("then owner can update any field")
    public void whenAllFieldsUpdatedByOwner_thenUpdateUser_success() {
        // given
        Long userId = existingUser1.getId();

        String newFirstName = "newFirstName";
        String newLastName = "newLastName";
        String newUserName = "newUserName";
        String newPassword = "newPassword";

        // when
        tested.updateUser(User.builder()
                        .id(userId)
                        .firstName(newFirstName)
                        .lastName(newLastName)
                        .userName(newUserName)
                        .password(newPassword).build()
                , existingUser1.getUserName()
        );

        // then
        Optional<UserEntity> result = userRepository.findById(userId);
        assertThat(result).isPresent().get()
                .satisfies(r -> assertAll("Update User Result",
                        () -> assertThat(r.getFirstName()).isEqualTo(newFirstName),
                        () -> assertThat(r.getLastName()).isEqualTo(newLastName),
                        () -> assertThat(r.getUserName()).isEqualTo(newUserName),
                        () -> assertThat(r.getPassword()).isEqualTo(newPassword)
                ));

    }

    @Test
    @DisplayName("change user name by owner possible")
    public void whenExistingUserChangeUserName_thenUpdateUser_success() {
        // given
        Long userId = existingUser1.getId();

        String newUserName = "newUserName";

        // when
        tested.updateUser(User.builder()
                        .id(userId)
                        .userName(newUserName).build()
                , existingUser1.getUserName()
        );

        // then
        Optional<UserEntity> result = userRepository.findById(userId);
        assertThat(result).isPresent().get()
                .satisfies(r -> assertAll("Update User Result",
                        () -> assertThat(r.getUserName()).isEqualTo(newUserName),
                        () -> assertThat(r.getFirstName()).isEqualTo(existingUser1.getFirstName()),
                        () -> assertThat(r.getLastName()).isEqualTo(existingUser1.getLastName()),
                        () -> assertThat(r.getPassword()).isEqualTo(existingUser1.getPassword())
                ));

    }

    @Test
    @DisplayName("non owner update user throws exception")
    public void whenIrrelevantUser_thenUpdateUser_fail() {

        var userId = existingUser1.getId();
        var userName = existingUser1.getUserName();

        assertThrows(
                UnauthorizedException.class, () -> tested.updateUser(
                        User.builder().id(userId)
                                .userName(userName).build()
                        ,"NON OWNER USER"));


    }

    @Test
    @DisplayName("then delete by id 1 deletes user 1")
    public void whenExistingUser_thenDeleteById_success() {

        var id= existingUser1.getId();

        // when
        tested.deleteUser(id);

        // then
        assertThrows(UserNotFoundException.class, () -> tested.getUserById(id));
    }

    @Test
    @DisplayName("then delete by non persisted id silently fails")
    public void whenNonExistingUser_thenDeleteById_silenFail() {

        var id= 999L;

        // when
        tested.deleteUser(id);

    }

}
