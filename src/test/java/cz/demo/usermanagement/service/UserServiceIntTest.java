package cz.demo.usermanagement.service;


import cz.demo.usermanagement.exception.UserAlreadyExistsException;
import cz.demo.usermanagement.exception.UserNotFoundException;
import cz.demo.usermanagement.mapper.UserMapper;
import cz.demo.usermanagement.repository.UserDAO;
import cz.demo.usermanagement.repository.UserRepository;
import cz.demo.usermanagement.repository.entity.User;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Tag("integration-test")
@DisplayName("Given user service with 2 users")
class UserServiceIntTest {

    @Autowired
    private UserDAO userDAO;

    /**
     * For testing purposes, needed in @BeforeEach, @AfterEach as they dont support @Transactional
     */
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder encoder;

    @Autowired
    private UserServiceImpl tested;

    @Autowired
    private UserMapper userMapper;

    private User existingUser1;
    private User existingUser2;

    @BeforeEach
    void setUp() {
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

        userRepository.save(existingUser1);
        userRepository.save(existingUser2);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteById(existingUser1.getId());
        userRepository.deleteById(existingUser2.getId());
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

            Optional<User> result = userDAO.findById(user.getId());
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

            assertThrows(UserAlreadyExistsException.class, () -> tested.createUser(existingUser1));

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
            tested.deleteUser(id, existingUser1.getUserName());

            // then
            assertThat(userDAO.findById(id))
                    .isNotPresent();
        }

        @Test
        @DisplayName("non owner delete user unauthorized error")
        void whenIrrelevantUser_thenDeleteUser_fail() {

            var badUser = "badUser";

            assertThrows(
                    AccessDeniedException.class,
                    () -> tested.deleteUser(existingUser1.getId(), badUser));


        }

        @Test
        @DisplayName("non existing user throws exception")
        void whenNonExistingUser_thenDeleteById_throwsException() {

            assertThrows(UserNotFoundException.class, () -> tested.deleteUser(999, "some-user"));

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
            Optional<User> result = userDAO.findById(userId);
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
            Optional<User> result = userDAO.findById(userId);
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
                    AccessDeniedException.class,
                    () -> tested.updateUser(user,badUser));


        }

    }

}
