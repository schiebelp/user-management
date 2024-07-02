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

        user = User.builder()
                .id(1)
                .firstName("John")
                .lastName("Cena")
                .userName("johncena1")
                .password("passwordTest")
                .build();

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
            ResultActions response = mvc.perform(post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(user)));

            // then
            response.andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", is(String.valueOf(user.getId()))))
                    .andExpect(jsonPath("$.userName", is(user.getUserName())))
                    .andExpect(jsonPath("$.firstName", is(user.getFirstName())))
                    .andExpect(jsonPath("$.lastName", is(user.getLastName())));
                    // password hidden!

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
            ResultActions response = mvc.perform(post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(user)));

            // then
            response.andDo(print()).
                    andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.type", is("about:blank")))
                    .andExpect(jsonPath("$.title", is(HttpStatus.BAD_REQUEST.getReasonPhrase())))
                    .andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value())))
                    .andExpect(jsonPath("$.detail", is("[Password is mandatory]"))) // Expecting "Password is mandatory"
                    .andExpect(jsonPath("$.instance", is("/api/users")));

            verifyNoInteractions(userService);
        }

        @Test
        @DisplayName("400 Bad Request: Name is mandatory")
        void givenNoUserName_whenPost_thenMandatoryErr() throws Exception{
            // given
            user.setUserName(null);

            // when
            ResultActions response = mvc.perform(post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(user)));

            // then
            response.andDo(print()).
                    andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.type", is("about:blank")))
                    .andExpect(jsonPath("$.title", is(HttpStatus.BAD_REQUEST.getReasonPhrase())))
                    .andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value())))
                    .andExpect(jsonPath("$.detail", is("[Username is mandatory]"))) // Expecting [Name is mandatory]
                    .andExpect(jsonPath("$.instance", is("/api/users")));

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
            ResultActions response = mvc.perform(get("/api/users/{id}", id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(user)));

            // then
            response.andDo(print()).
                    andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(String.valueOf(user.getId()))))
                    .andExpect(jsonPath("$.userName", is(user.getUserName())))
                    .andExpect(jsonPath("$.firstName", is(user.getFirstName())))
                    .andExpect(jsonPath("$.lastName", is(user.getLastName())));

            verify(userService).getUserById(id);
        }

        @Test
        @DisplayName("404 Not Found")
        void givenNonExistingUserId_whenGetById_thenNotFound() throws Exception{
            // given
            var id = -1;

            given(userService.getUserById(any(Integer.class)))
                    .willThrow(new UserNotFoundException(String.valueOf(id)));

            // when
            ResultActions response = mvc.perform(get("/api/users/{id}", id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(user)));

            // then
            response.andDo(print()).
                    andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.type", is("about:blank")))
                    .andExpect(jsonPath("$.title", is(HttpStatus.NOT_FOUND.getReasonPhrase())))
                    .andExpect(jsonPath("$.status", is(HttpStatus.NOT_FOUND.value())))
                    .andExpect(jsonPath("$.detail", is("-1")))
                    .andExpect(jsonPath("$.instance", is("/api/users/-1")));

            verify(userService).getUserById(id);
        }

        @Test
        @DisplayName("400 Invalid ID supplied")
        void givenInvalidId_whenGetById_badRequest() throws Exception{
            // given
            var id = "invalidId";

            // when
            ResultActions response = mvc.perform(get("/api/users/{id}", id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(user)));

            // then
            response.andDo(print()).
                    andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.type", is("about:blank")))
                    .andExpect(jsonPath("$.title", is(HttpStatus.BAD_REQUEST.getReasonPhrase())))
                    .andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value())))
                    .andExpect(jsonPath("$.detail", is("Invalid 'id' supplied. Should be a valid 'Integer' and 'invalidId' isn't!")))
                    .andExpect(jsonPath("$.instance", is("/api/users/invalidId")));

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
            ResultActions response = mvc.perform(get("/api/users")
                    .contentType(MediaType.APPLICATION_JSON));

            // then
            response.andDo(print())
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
            ResultActions response = mvc.perform(delete("/api/users/{id}", id)
                    .contentType(MediaType.APPLICATION_JSON));

            // then
            response.andDo(print())
                    .andExpect(status().isOk());

            verify(userService).deleteUser(id);
        }

        @Test
        @DisplayName("404 Not Found")
        void givenNonExistingUserId_whenDelete_thenNotFound() throws Exception {
            // given
            Integer id = -1;

            doThrow(new UserNotFoundException(String.valueOf(id)))
                    .when(userService)
                    .deleteUser(any(Integer.class));

            // when
            ResultActions response = mvc.perform(delete("/api/users/{id}", id)
                    .contentType(MediaType.APPLICATION_JSON));

            // then
            response.andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.type", is("about:blank")))
                    .andExpect(jsonPath("$.title", is(HttpStatus.NOT_FOUND.getReasonPhrase())))
                    .andExpect(jsonPath("$.status", is(HttpStatus.NOT_FOUND.value())))
                    .andExpect(jsonPath("$.detail", is("-1")))
                    .andExpect(jsonPath("$.instance", is("/api/users/-1")));

            verify(userService).deleteUser(id);
        }
    }

    @Nested
    @DisplayName("PUT /users/{id} : Update an existing user")
    class UpdateUser {

        @Test
        @DisplayName("200 OK")
        void givenValidUser_whenPut_thenUpdated() throws Exception {
            // given
            var newUserName = "newUserName"; // <---the update
            var id = user.getId();
            var loggedUserName = "admin";

            var request = User.builder()
                            .id(id)
                            .userName(newUserName).build();

            var result = User.builder()
                            .id(user.getId())
                            .userName(newUserName)
                            .firstName(user.getFirstName())
                            .lastName(user.getLastName()).build();

            given(userService.updateUser(any(User.class), anyString())).willReturn(result);

            // when
            ResultActions response = mvc.perform(put("/api/users/{id}", id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // then
            response.andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(String.valueOf(result.getId()))))
                    .andExpect(jsonPath("$.userName", is(newUserName))) // <---the update
                    .andExpect(jsonPath("$.firstName", is(result.getFirstName())))
                    .andExpect(jsonPath("$.lastName", is(result.getLastName())));
                    // password hidden!

            verify(userService).updateUser(userArgumentCaptor.capture(), eq(loggedUserName));

            User captured = userArgumentCaptor.getValue();
            assertAll("Updated User",
                    () -> assertThat(captured.getId()).isEqualTo(id),
                    () -> assertThat(captured.getUserName()).isEqualTo(newUserName) // <---the update
            );

        }

        @Test
        @DisplayName("200 OK: ")
        void givenNoUsername_whenPut_thenNoChange() throws Exception {
            // given
            var id = user.getId();
            var loggedUserName = "admin";

            var request = User.builder()
                    .id(id)
                    .userName(null) // <-- the update
                    .password(user.getPassword())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName()).build();

            given(userService.updateUser(any(User.class), anyString())).willReturn(user);

            // when
            ResultActions response = mvc.perform(put("/api/users/{id}", id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))); // <-- the change

            // then
            response.andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(String.valueOf(user.getId()))))
                    .andExpect(jsonPath("$.userName", is(user.getUserName()))) // <-- the change aborted
                    .andExpect(jsonPath("$.firstName", is(user.getFirstName())))
                    .andExpect(jsonPath("$.lastName", is(user.getLastName())));
                    // password hidden!

            verify(userService).updateUser(userArgumentCaptor.capture(), eq(loggedUserName));

            User captured = userArgumentCaptor.getValue();
            assertAll("Request User",
                    () -> assertThat(captured.getId()).isEqualTo(user.getId()),
                    () -> assertThat(captured.getFirstName()).isEqualTo(user.getFirstName()),
                    () -> assertThat(captured.getLastName()).isEqualTo(user.getLastName()),
                    () -> assertThat(captured.getUserName()).isNull(), // <-- the change
                    () -> assertThat(captured.getPassword()).isEqualTo(user.getPassword())
            );
        }

        @Test
        @DisplayName("404 Not Found: User not found with given id")
        void givenNonExistingId_whenPut_thenNotFoundException() throws Exception {
            // given
            var wrongId = 99;
            var loggedUserName = "admin";

            var request = User.builder()
                    .id(wrongId)
                    .userName(user.getUserName())
                    .password(user.getPassword())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName()).build();

            given(userService.updateUser(any(User.class), anyString()))
                    .willThrow(new UserNotFoundException(String.valueOf(wrongId)));

            // when
            ResultActions response = mvc.perform(put("/api/users/{id}", wrongId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // then
            response.andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.type", is("about:blank")))
                    .andExpect(jsonPath("$.title", is(HttpStatus.NOT_FOUND.getReasonPhrase())))
                    .andExpect(jsonPath("$.status", is(HttpStatus.NOT_FOUND.value())))
                    .andExpect(jsonPath("$.detail", is(String.valueOf(wrongId))))
                    .andExpect(jsonPath("$.instance", is("/api/users/" + wrongId)));

            verify(userService).updateUser(userArgumentCaptor.capture(), eq(loggedUserName));

            User captured = userArgumentCaptor.getValue();
            assertAll("Request User",
                    () -> assertThat(captured.getId()).isEqualTo(wrongId),
                    () -> assertThat(captured.getFirstName()).isEqualTo(user.getFirstName()),
                    () -> assertThat(captured.getLastName()).isEqualTo(user.getLastName()),
                    () -> assertThat(captured.getUserName()).isEqualTo(user.getUserName()),
                    () -> assertThat(captured.getPassword()).isEqualTo(user.getPassword())
            );
        }
    }

}
