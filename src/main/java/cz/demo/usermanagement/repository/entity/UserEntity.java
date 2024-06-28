package cz.demo.usermanagement.repository.entity;


import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
@Entity
@Table(name="\"User\"") // reserved keyword in postgre
public class UserEntity {

    // atributy id a name, username, password, role
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userName;

    private String password;

    private String firstName;

    private String lastName;

//    private String role; //todo
}

