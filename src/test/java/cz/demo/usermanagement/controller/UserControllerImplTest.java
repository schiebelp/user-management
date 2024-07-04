package cz.demo.usermanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.demo.usermanagement.exception.UserNotFoundException;
import cz.demo.usermanagement.service.UserService;
import cz.demo.usermanagement.service.domain.User;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web layer tests with JUnit 5 and Mockito
 */
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "admin", password = "password")
class UserControllerImplTest {

    private static final String API_USERS = "/api/users";
    private static final String USERNAME_IS_MANDATORY = "[Username is mandatory]";
    private static final String PASSWORD_IS_MANDATORY = "[Password is mandatory]";
    private static final String ABOUT_BLANK = "about:blank";

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Captor
    private ArgumentCaptor<User> userArgumentCaptor;

    User user;

    @BeforeEach
    public void setup(){

        user = createUser(1, "johncena1", "passwordTest", "John", "Cena");

    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(userService);
    }

    @Nested
    @DisplayName("POST /users : Create a new user")
    class CreateUser{

        @Test
        @DisplayName("201 Created")
        void givenUser_whenPost_thenIsCreated() throws Exception{
            // given
            given(userService.createUser(any(User.class))).willReturn(user);

            // when
            ResultActions result = performPost(user);

            // then
            expectSuccess(result, status().isCreated(), user);

            verify(userService).createUser(userArgumentCaptor.capture());

            User captured = userArgumentCaptor.getValue();
            assertAll("Request User",
                    () -> assertThat(captured.getId()).isNull(),
                    () -> assertThat(captured.getFirstName()).isEqualTo(user.getFirstName()),
                    () -> assertThat(captured.getLastName()).isEqualTo(user.getLastName()),
                    () -> assertThat(captured.getUserName()).isEqualTo(user.getUserName()),
                    () -> assertThat(captured.getPassword()).isEqualTo(user.getPassword())
            );
        }

        @Test
        @DisplayName("400 Bad Request: Password is mandatory")
        void givenNoPassword_whenPost_thenMandatoryErr() throws Exception{
            // given
            user.setPassword(null);

            // when
            ResultActions response = performPost(user);

            // then
            expectBadRequest(response, PASSWORD_IS_MANDATORY, API_USERS);

            verifyNoInteractions(userService);
        }

        @Test
        @DisplayName("400 Bad Request: Name is mandatory")
        void givenNoUserName_whenPost_thenMandatoryErr() throws Exception{
            // given
            user.setUserName(null);

            // when
            ResultActions response = performPost(user);

            // then
            expectBadRequest(response, USERNAME_IS_MANDATORY, API_USERS);

            verifyNoInteractions(userService);
        }
    }

    @Nested
    @DisplayName("GET /users/{id} : Get a user by ID")
    class GetUserById{

        @Test
        @DisplayName("200 OK")
        void givenUser_whenGetById_thenReturned() throws Exception{
            // given
            var id = user.getId();

            given(userService.getUserById(any(Integer.class))).willReturn(user);

            // when
            ResultActions result = performGetById(id);

            // then
            expectSuccess(result, status().isOk(), user);

            verify(userService).getUserById(id);
        }

        @Test
        @DisplayName("404 Not Found")
        void givenNonExistingUserId_whenGetById_thenNotFound() throws Exception{
            // given
            String id = "-1";

            given(userService.getUserById(any(Integer.class)))
                    .willThrow(new UserNotFoundException(id));

            // when
            ResultActions response = performGetById(id);

            // then
            expectNotFound(response, id);

            verify(userService).getUserById(-1);
        }

        @Test
        @DisplayName("400 Invalid ID supplied")
        void givenInvalidId_whenGetById_badRequest() throws Exception{
            // given
            var id = "invalidId";

            // when
            ResultActions response = performGetById(id);

            // then
            expectBadRequest(response,
                    "Invalid 'id' supplied. Should be a valid 'Integer' and 'invalidId' isn't!",
                    API_USERS + "/"+ id);

            verifyNoInteractions(userService);
        }

    }

    @Nested
    @DisplayName("GET /users : Get all users")
    class GetAllUsers {

        @Test
        @DisplayName("200 OK")
        void givenUsers_whenGetAll_thenSuccess() throws Exception {
            // given
            given(userService.getAllUsers()).willReturn(Collections.singletonList(user));

            // when
            performGetAll().andDo(print())
                    // then
                    .andExpect(status().isOk())
                        .andExpect(jsonPath("$[0].id", is(String.valueOf(user.getId()))))
                        .andExpect(jsonPath("$[0].userName", is(user.getUserName())))
                        .andExpect(jsonPath("$[0].firstName", is(user.getFirstName())))
                        .andExpect(jsonPath("$[0].lastName", is(user.getLastName())));

            verify(userService).getAllUsers();
        }
    }

    @Nested
    @DisplayName("DELETE /users/{id} : Delete a user by ID")
    class DeleteUser {

        @Test
        @DisplayName("200 OK")
        void givenUserId_whenDelete_thenOK() throws Exception {
            // given
            Integer id = user.getId();

            // when
            performDelete(id).andDo(print())
                    // then
                    .andExpect(status().isOk());

            verify(userService).deleteUser(id);
        }

        @Test
        @DisplayName("404 Not Found")
        void givenNonExistingUserId_whenDelete_thenNotFound() throws Exception {
            // given
            String id = "-1";

            doThrow(new UserNotFoundException(id))
                    .when(userService)
                    .deleteUser(any(Integer.class));

            // when
            ResultActions response = performDelete(id);

            // then
            expectNotFound(response, id);

            verify(userService).deleteUser(-1);
        }
    }

    @Nested
    @DisplayName("PUT /users/{id} : Update an existing user")
    class UpdateUser {

        @Test
        @DisplayName("200 OK: User updated")
        void givenValidUser_whenPut_thenUpdated() throws Exception {
            // given
            var id = user.getId();
            var newUserName = "newUserName"; // <---the update
            var loggedUserName = "admin";

            var request = User.builder().id(id).userName(newUserName).build();
            var response = createUser(id, newUserName, user.getPassword(), user.getFirstName(), user.getLastName());

            given(userService.updateUser(any(User.class), anyString())).willReturn(response);

            // when
            ResultActions result = performPut(id, request);

            // then
            expectSuccess(result, status().isOk(), response);

            verify(userService).updateUser(userArgumentCaptor.capture(), eq(loggedUserName));

            User captured = userArgumentCaptor.getValue();
            assertAll("Updated User",
                    () -> assertThat(captured.getId()).isEqualTo(id),
                    () -> assertThat(captured.getUserName()).isEqualTo(newUserName) // <---the update
            );

        }

        @Test
        @DisplayName("200 OK: No change on empty")
        void givenNoUsername_whenPut_thenNoChange() throws Exception {
            // given
            var loggedUserName = "admin";
            var request = createUser(user.getId(), null, user.getPassword(), user.getFirstName(), user.getLastName());

            given(userService.updateUser(any(User.class), anyString())).willReturn(user);

            // when
            ResultActions result = performPut(user.getId(), request);

            // then
            expectSuccess(result, status().isOk(), user);

            verifyUpdatedUser(loggedUserName, user.getId(), null, user.getPassword(),
                    user.getFirstName(), user.getLastName());
        }

        @Test
        @DisplayName("404 Not Found: User not found with given id")
        void givenNonExistingId_whenPut_thenNotFoundException() throws Exception {
            // given
            var wrongId = 99;
            var loggedUserName = "admin";

            var request = createUser(wrongId, user.getUserName(), user.getPassword(), user.getFirstName(), user.getLastName());

            given(userService.updateUser(any(User.class), anyString()))
                    .willThrow(new UserNotFoundException(String.valueOf(wrongId)));

            // when
            ResultActions response = performPut(wrongId, request);

            // then
            expectNotFound(response, String.valueOf(wrongId));

            verifyUpdatedUser(loggedUserName, wrongId, user.getUserName(), user.getPassword(),
                    user.getFirstName(), user.getLastName());
        }
    }

    // Helper methods ------------------------------------------------------------
    private ResultActions performPost(User user) throws Exception {
        return mvc.perform(post(API_USERS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)));
    }

    private ResultActions performGetById(Object id) throws Exception {
        return mvc.perform(get(API_USERS + "/{id}", id)
                .contentType(MediaType.APPLICATION_JSON));
    }

    private ResultActions performGetAll() throws Exception {
        return mvc.perform(get(API_USERS)
                .contentType(MediaType.APPLICATION_JSON));
    }

    private ResultActions performDelete(Object id) throws Exception {
        return mvc.perform(delete(API_USERS + "/{id}", id)
                .contentType(MediaType.APPLICATION_JSON));
    }

    private ResultActions performPut(int id, User request) throws Exception {
        return mvc.perform(put(API_USERS + "/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    private void expectBadRequest(ResultActions response, String errorMessage, String path) throws Exception {
        response.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type", is(ABOUT_BLANK)))
                .andExpect(jsonPath("$.title", is(HttpStatus.BAD_REQUEST.getReasonPhrase())))
                .andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath("$.detail", is(errorMessage)))
                .andExpect(jsonPath("$.instance", is(path)));
    }

    private void expectNotFound(ResultActions response, String id) throws Exception {
        response.andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type", is(ABOUT_BLANK)))
                .andExpect(jsonPath("$.title", is(HttpStatus.NOT_FOUND.getReasonPhrase())))
                .andExpect(jsonPath("$.status", is(HttpStatus.NOT_FOUND.value())))
                .andExpect(jsonPath("$.detail", is(id)))
                .andExpect(jsonPath("$.instance", is(API_USERS + "/" + id)));
    }

    private User createUser(int id, String userName, String password, String firstName, String lastName) {
        return User.builder()
                .id(id)
                .userName(userName)
                .password(password)
                .firstName(firstName)
                .lastName(lastName)
                .build();
    }

    private void expectSuccess(ResultActions response, ResultMatcher matcher, User user) throws Exception {
        response.andDo(print())
                .andExpect(matcher)
                .andExpect(jsonPath("$.id", is(String.valueOf(user.getId()))))
                .andExpect(jsonPath("$.userName", is(user.getUserName())))
                .andExpect(jsonPath("$.firstName", is(user.getFirstName())))
                .andExpect(jsonPath("$.lastName", is(user.getLastName())));
                 // password hidden!
    }

    private void verifyUpdatedUser(String loggedUserName, int id, String userName, String password,
            String firstName, String lastName) {
        verify(userService).updateUser(userArgumentCaptor.capture(), eq(loggedUserName));

        User captured = userArgumentCaptor.getValue();

        assertAll("Updated User",
                () -> assertThat(captured.getId()).isEqualTo(id),
                () -> assertThat(captured.getUserName()).isEqualTo(userName),
                () -> assertThat(captured.getPassword()).isEqualTo(password),
                () -> assertThat(captured.getFirstName()).isEqualTo(firstName),
                () -> assertThat(captured.getLastName()).isEqualTo(lastName)
        );
    }

}
