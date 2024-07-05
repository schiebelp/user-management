package cz.demo.usermanagement.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.validator.constraints.Length;

/**
 * DB class of User
 *
 * nice to have: roles
 */
@Data
@Entity
@Builder
@Table(name="\"User\"") // reserved keyword in postgre
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String userName;

    @Column(nullable = false)
    private String password;

    @NotNull
    @Length(max = 50)
    private String firstName;

    @NotNull
    @Length(max = 255)
    private String lastName;

    @ToString.Include(name = "password")
    public String getPasswordMasked() {
        return password != null ? "***" : "null";
    }

}

