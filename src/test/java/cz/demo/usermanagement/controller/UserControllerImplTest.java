package cz.demo.usermanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.demo.usermanagement.exception.UserAccessDeniedException;
import cz.demo.usermanagement.exception.UserAlreadyExistsException;
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
import static org.hamcrest.Matchers.containsString;
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
    private static final String ABOUT_BLANK = "about:blank";
    private static final String USER_NAME_TOO_LONG = "ThisTextIs31CharactersLong00fjd";
    private static final String USER_NAME_TOO_SHORT = "xy";

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Captor
    private ArgumentCaptor<User> userArgumentCaptor;

    private User user;

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
        @DisplayName("400 Bad Request: User Name, Password is mandatory") //must not be blank
        void givenNoPassword_whenPost_thenMandatoryErr() throws Exception{
            // given
            user.setUserName(null);
            user.setPassword(null);

            // when
            ResultActions response = performPost(user);

            // then
            expectBadRequest(response, API_USERS, "User Name is mandatory", "Password is mandatory");

            verifyNoInteractions(userService);
        }

        @Test
        @DisplayName("400 Bad Request: User Name - SQL Injection  caught")
        void givenSqlInjectionUserName_whenPost_thenSizeErr() throws Exception{
            // given
            user.setUserName("DROP ALL TABLES;");

            // when
            ResultActions response = performPost(user);

            // then
            expectBadRequest(response, API_USERS, "Username must contain only letters, numbers, underscore, or hyphen");

            verifyNoInteractions(userService);
        }

        @Test
        @DisplayName("400 Bad Request: Short User Name size must be between ...")
        void givenShortUserName_whenPost_thenSizeErr() throws Exception{
            // given
            user.setUserName(USER_NAME_TOO_SHORT);

            // when
            ResultActions response = performPost(user);

            // then
            expectBadRequest(response, API_USERS, "User Name size must be between 3 and 30");

            verifyNoInteractions(userService);
        }

        @Test
        @DisplayName("400 Bad Request: Long User Name size must be between ...")
        void givenLongUserName_whenPost_thenSizeErr() throws Exception{
            // given
            user.setUserName(USER_NAME_TOO_LONG);

            // when
            ResultActions response = performPost(user);

            // then
            expectBadRequest(response, API_USERS, "User Name size must be between 3 and 30");

            verifyNoInteractions(userService);
        }

        @Test
        @DisplayName("409 Conflict: User already exists")
        void givenExistingUser_whenCreateUser_thenConflict() throws Exception {

            given(userService.createUser(any(User.class)))
                    .willThrow(new UserAlreadyExistsException("some-explanation"));

            ResultActions response = performPost(user);

            expectConflict(response, "some-explanation");

            verify(userService).createUser(any(User.class));

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
            expectBadRequest(response, API_USERS + "/"+ id,
                    "Invalid 'id' supplied. Should be a valid 'Integer' and 'invalidId' isn't!"
                    );

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

            verify(userService).deleteUser(id, "admin");
        }

        @Test
        @DisplayName("404 Not Found")
        void givenNonExistingUserId_whenDelete_thenNotFound() throws Exception {
            // given
            String id = "-1";

            doThrow(new UserNotFoundException(id))
                    .when(userService)
                    .deleteUser(any(Integer.class), anyString());

            // when
            ResultActions response = performDelete(id);

            // then
            expectNotFound(response, id);

            verify(userService).deleteUser(-1, "admin");
        }

        @Test
        @DisplayName("403 Forbidden: Access denied: Only admin or same user can delete")
        @WithMockUser(username = "non-author", password = "password")
        void givenNonAuthorUser_whenDelete_thenDenied() throws Exception {

            doThrow(new UserAccessDeniedException("Reason"))
                    .when(userService)
                    .deleteUser(any(Integer.class), anyString());

            // when
            ResultActions response = performDelete(user.getId());

            // then
            expectForbidden(response, user.getId(), "Reason");

            verify(userService).deleteUser(user.getId(), "non-author");
        }
    }

    @Nested
    @DisplayName("PUT /users/{id} : Update an existing user")
    class UpdateUser {


        @Test
        @DisplayName("200 OK: User updated")
        void givenFullUser_whenPut_thenUpdated() throws Exception {
            // given
            var id = user.getId();
            var request = createUser(user.getId(), "putUsername", "putPassword", "putFirstname", "putLastName");

            given(userService.updateUser(any(User.class), anyString())).willReturn(request);

            // when
            ResultActions result = performPut(id, request);

            // then
            expectSuccess(result, status().isOk(), request);

            verifyUpdatedUser("admin", request);

        }

        @Test
        @DisplayName("400 Bad Request: Password is mandatory")
        void givenNoPassword_whenPut_thenNoChange() throws Exception {
            // given
            user.setPassword(null);

            // when
            ResultActions response = performPut(user.getId(), user);

            // then
            expectBadRequest(response, API_USERS + "/" + user.getId(), "Password is mandatory");

            verifyNoInteractions(userService);
        }

        @Test
        @DisplayName("400 Bad Request: User Name is mandatory")
        void givenNoUserName_whenPut_thenNoChange() throws Exception {
            // given
            user.setUserName(null);

            // when
            ResultActions response = performPut(user.getId(), user);

            // then
            expectBadRequest(response, API_USERS + "/" + user.getId(), "User Name is mandatory");

            verifyNoInteractions(userService);
        }

        @Test
        @DisplayName("400 Bad Request: User Name size must be between ...")
        void givenShortUserName_whenPut_thenSizeErr() throws Exception{
            // given
            user.setUserName(USER_NAME_TOO_SHORT);

            // when
            ResultActions response = performPut(user.getId(), user);

            // then
            expectBadRequest(response, API_USERS + "/" + user.getId(), "User Name size must be between 3 and 30");

            verifyNoInteractions(userService);
        }


        @Test
        @DisplayName("400 Bad Request: Password size must be between ...")
        void givenShortPassword_whenPut_thenSizeErr() throws Exception{
            // given
            user.setPassword("short");

            // when
            ResultActions response = performPut(user.getId(), user);

            // then
            expectBadRequest(response, API_USERS + "/" + user.getId(), "Password size must be between 8 and 72" );

            verifyNoInteractions(userService);
        }

        @Test
        @DisplayName("404 Not Found: User not found with given id")
        void givenNonExistingId_whenPut_thenNotFoundException() throws Exception {
            // given
            var nonExistId = 99;
            user.setId(nonExistId);

            given(userService.updateUser(any(User.class), anyString()))
                    .willThrow(new UserNotFoundException(String.valueOf(nonExistId)));

            // when
            ResultActions response = performPut(nonExistId, user);

            // then
            expectNotFound(response, String.valueOf(nonExistId));

            verifyUpdatedUser("admin", user);
        }

        @Test
        @DisplayName("403 Forbidden: Access denied: Only admin or same user can update")
        @WithMockUser(username = "non-author", password = "password")
        void givenNonAuthorUser_whenPut_thenDenied() throws Exception {

            given(userService.updateUser(any(User.class), anyString()))
                    .willThrow(new UserAccessDeniedException("Reason"));

            // when
            ResultActions response = performPut(user.getId(), user);

            // then
            expectForbidden(response, user.getId(), "Reason");

            verifyUpdatedUser("non-author", user);
        }

    }

    @Nested
    @DisplayName("PATCH /users/{id} : Partially update an existing user")
    class PartiallyUpdateUser {

        @Test
        @DisplayName("200 OK: User partialy updated")
        void givenValidUser_whenPatch_thenUpdated() throws Exception {
            // given
            var id = user.getId();
            var newUserName = "newUserName"; // <---the update

            var request = User.builder().id(id).userName( "newUserName").build();
            var response = createUser(id, newUserName, user.getPassword(), user.getFirstName(), user.getLastName());

            given(userService.updateUser(any(User.class), anyString())).willReturn(response);

            // when
            ResultActions result = performPatch(id, request);

            // then
            expectSuccess(result, status().isOk(), response);

            verify(userService).updateUser(userArgumentCaptor.capture(), eq("admin"));

            User captured = userArgumentCaptor.getValue();
            assertAll("Updated User",
                    () -> assertThat(captured.getId()).isEqualTo(id),
                    () -> assertThat(captured.getUserName()).isEqualTo(newUserName) // <---the update
            );

        }

        @Test
        @DisplayName("200 OK: No change on empty")
        void givenNoUsername_whenPatch_thenNoChange() throws Exception {
            // given
            var request = createUser(user.getId(), null, user.getPassword(), user.getFirstName(), user.getLastName());

            given(userService.updateUser(any(User.class), anyString())).willReturn(user);

            // when
            ResultActions result = performPatch(user.getId(), request);

            // then
            expectSuccess(result, status().isOk(), user);

            verifyUpdatedUser("admin", request);
        }

        @Test
        @DisplayName("400 Bad Request: User Name size must be between ...")
        void givenShortUserName_whenPatch_thenSizeErr() throws Exception{
            // given
            user.setUserName(USER_NAME_TOO_SHORT);

            // when
            ResultActions response = performPatch(user.getId(), user);

            // then
            expectBadRequest(response, API_USERS + "/" + user.getId(), "User Name size must be between 3 and 30");

            verifyNoInteractions(userService);
        }

        @Test
        @DisplayName("400 Bad Request: Password size must be between ...")
        void givenShortPassword_whenPatch_thenSizeErr() throws Exception{
            // given
            user.setPassword("short");

            // when
            ResultActions response = performPatch(user.getId(), user);

            // then
            expectBadRequest(response, API_USERS + "/" + user.getId(), "Password size must be between 8 and 72");

            verifyNoInteractions(userService);
        }

        @Test
        @DisplayName("404 Not Found: User not found with given id")
        void givenNonExistingId_whenPatch_thenNotFoundException() throws Exception {
            // given
            var wrongId = 99;

            var request = createUser(wrongId, user.getUserName(), user.getPassword(), user.getFirstName(), user.getLastName());

            given(userService.updateUser(any(User.class), anyString()))
                    .willThrow(new UserNotFoundException(String.valueOf(wrongId)));

            // when
            ResultActions response = performPatch(wrongId, request);

            // then
            expectNotFound(response, String.valueOf(wrongId));

            verifyUpdatedUser("admin", request);
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

    private ResultActions performPatch(int id, User request) throws Exception {
        return mvc.perform(patch(API_USERS + "/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    private void expectBadRequest(ResultActions response, String path, String ... errorMessages) throws Exception {
        response.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type", is(ABOUT_BLANK)))
                .andExpect(jsonPath("$.title", is(HttpStatus.BAD_REQUEST.getReasonPhrase())))
                .andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath("$.instance", is(path)));


        // Check if any of the error messages are contained in the $.detail field
        for (String errorMessage : errorMessages) {
            response.andExpect(jsonPath("$.detail", containsString(errorMessage)));
        }

    }

    private void expectConflict(ResultActions response, String message) throws Exception {
        response.andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type", is(ABOUT_BLANK)))
                .andExpect(jsonPath("$.title", is(HttpStatus.CONFLICT.getReasonPhrase())))
                .andExpect(jsonPath("$.status", is(HttpStatus.CONFLICT.value())))
                .andExpect(jsonPath("$.detail", is(message)))
                .andExpect(jsonPath("$.instance", is(API_USERS)));
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

    private void expectForbidden(ResultActions response, Integer id, String message) throws Exception {
        response.andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.type", is(ABOUT_BLANK)))
                .andExpect(jsonPath("$.title", is(HttpStatus.FORBIDDEN.getReasonPhrase())))
                .andExpect(jsonPath("$.status", is(HttpStatus.FORBIDDEN.value())))
                .andExpect(jsonPath("$.detail", is(message)))
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

    private void verifyUpdatedUser(String loggedUserName, User expected) {
        verify(userService).updateUser(userArgumentCaptor.capture(), eq(loggedUserName));

        User captured = userArgumentCaptor.getValue();

        assertThat(captured).usingRecursiveComparison().isEqualTo(expected);
    }

}
