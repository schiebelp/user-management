package cz.demo.usermanagement.controller.dto;

import lombok.Data;

/**
 * DTO for UserAPI
 */
@Data
public class GetUserResponse {
    private String id;
    private String userName;
    private String firstName;
    private String lastName;
}
