package cz.demo.usermanagement.service;


import cz.demo.usermanagement.exception.UnauthorizedException;
import cz.demo.usermanagement.exception.UserAlreadyExistsException;
import cz.demo.usermanagement.exception.UserNotFoundException;
import cz.demo.usermanagement.mapper.UserMapper;
import cz.demo.usermanagement.repository.UserRepository;
import cz.demo.usermanagement.repository.entity.UserEntity;
import cz.demo.usermanagement.service.domain.User;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Given user service with 2 users")
class UserServiceIntTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder encoder;

    @Autowired
    private UserServiceImpl tested;

    @Autowired
    private UserMapper userMapper;

    private UserEntity existingUser1;
    private UserEntity existingUser2;

    @BeforeEach
    void setUp() {
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
    void tearDown() {
        userRepository.deleteAll();
    }


    @Nested
    @DisplayName("Create a new user")
    class CreateUser{

        @Test
        @DisplayName("will save succesfully new user")
        void givenUser_whenCreateUser_thenIsCreated() {
            // given
            User newUser = User.builder()
                    .firstName("Jame")
                    .lastName("King")
                    .userName("jamesking")
                    .password("password6254").build();

            // when
            User user = tested.createUser(newUser);

            // then
            assertAll("User by ID",
                    () -> assertThat(user.getId()).isPositive(),
                    () -> assertThat(user.getFirstName()).isEqualTo(newUser.getFirstName()),
                    () -> assertThat(user.getLastName()).isEqualTo(newUser.getLastName()),
                    () -> assertThat(user.getUserName()).isEqualTo(newUser.getUserName()),
                    () -> assertThat(user.getPassword()).isNotBlank()// Assuming password is hashed and cannot be directly compared
            );

            Optional<UserEntity> result = userRepository.findById(user.getId());
            assertThat(result).isPresent().get()
                    .satisfies(r -> assertAll("Create User Result",
                            () -> assertThat(r.getFirstName()).isEqualTo(newUser.getFirstName()),
                            () -> assertThat(r.getLastName()).isEqualTo(newUser.getLastName()),
                            () -> assertThat(r.getUserName()).isEqualTo(newUser.getUserName()),
                            () -> assertThat(r.getPassword()).isNotBlank() // Assuming password is hashed and cannot be directly compared
                    ));

        }

        @Test
        @DisplayName("will throw exception if user already exists")
        void givenExistingUser_whenCreateUser_thenUserExistException() {

            User user = userMapper.toUser(existingUser1);
            assertThrows(UserAlreadyExistsException.class, () -> tested.createUser(user));

        }

    }

    @Nested
    @DisplayName("Get a user by ID")
    class GetUserById{

        @Test
        @DisplayName("then get user by id finds user")
        void whenUserExist_thenGetUserById_success() {
            // given
            Integer userId = existingUser1.getId();

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
        void whenUserDontExist_thenGetUserById_throwsException() {

            assertThrows(UserNotFoundException.class, () -> tested.getUserById(-1));
        }

    }

    @Nested
    @DisplayName("Get all users")
    class GetAllUsers{

        @Test
        @DisplayName("returns all 2 users")
        void whenUsersExist_thenGetAllUsers_finds() {
            // Act
            List<User> users = tested.getAllUsers();

            // Assert
            assertThat(users).hasSize(2);

            assertThat(users)
                    .extracting(User::getUserName)
                    .containsExactlyInAnyOrder(existingUser1.getUserName(), existingUser2.getUserName() );
        }

    }

    @Nested
    @DisplayName("Delete a user by ID")
    class DeleteUser{

        @Test
        @DisplayName("then delete user by id removes user")
        void whenExistingUser_thenDeleteById_success() {

            var id= existingUser1.getId();

            // when
            tested.deleteUser(id);

            // then
            assertThat(userRepository.findById(id))
                    .isNotPresent();
        }

        @Test
        @DisplayName("non existing user throws exception")
        void whenNonExistingUser_thenDeleteById_throwsException() {

            assertThrows(UserNotFoundException.class, () -> tested.deleteUser(999));

        }

    }

    @Nested
    @DisplayName("Update an existing user")
    class UpdateUser{

        @Test
        @DisplayName("owner can update any field")
        void whenAllFieldsUpdatedByOwner_thenUpdateUser_success() {
            // given
            Integer userId = existingUser1.getId();
            String ownerName = existingUser1.getUserName();

            User update = User.builder()
                    .id(userId)
                    .firstName("newFirstName")
                    .lastName("newLastName")
                    .userName("newUserName")
                    .password("newPassword").build();

            // when
            tested.updateUser(update, ownerName);

            // then
            Optional<UserEntity> result = userRepository.findById(userId);
            assertThat(result).isPresent().get()
                    .satisfies(r -> assertAll("Update User Result",
                            () -> assertThat(r.getFirstName()).isEqualTo(update.getFirstName()),
                            () -> assertThat(r.getLastName()).isEqualTo(update.getLastName()),
                            () -> assertThat(r.getUserName()).isEqualTo(update.getUserName()),
                            () -> assertThat(r.getPassword()).isNotBlank()
                    ));

        }

        @Test
        @DisplayName("change only user name by owner possible")
        void whenExistingUserChangeUserName_thenUpdateUser_success() {
            // given
            Integer userId = existingUser1.getId();

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
        @DisplayName("non owner update user unauthorized error")
        void whenIrrelevantUser_thenUpdateUser_fail() {

            var badUser = "badUser";

            var user = User.builder()
                    .id(existingUser1.getId())
                    .userName(existingUser1.getUserName())
                    .build();

            assertThrows(
                    UnauthorizedException.class,
                    () -> tested.updateUser(user,badUser));


        }

    }

}
