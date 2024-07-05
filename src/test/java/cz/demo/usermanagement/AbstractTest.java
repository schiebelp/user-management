package cz.demo.usermanagement;

import cz.demo.usermanagement.mapper.UserMapper;
import cz.demo.usermanagement.model.User;
import cz.demo.usermanagement.model.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
public abstract class AbstractTest {

    @Autowired
    protected UserMapper userMapper;

    protected User createUser(Integer id, String userName, String password, String firstName, String lastName) {
        return User.builder()
                .id(id)
                .userName(userName)
                .password(password)
                .firstName(firstName)
                .lastName(lastName)
                .build();
    }

    protected UserEntity createUserEntiy(Integer id, String userName, String password, String firstName, String lastName) {
        return UserEntity.builder()
                .id(id)
                .userName(userName)
                .password(password)
                .firstName(firstName)
                .lastName(lastName)
                .build();
    }

}
