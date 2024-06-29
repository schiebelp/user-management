package cz.demo.usermanagement.service.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class User {

    private Long id;
    private String userName;
    private String password;

    private String firstName;
    private String lastName;

}
