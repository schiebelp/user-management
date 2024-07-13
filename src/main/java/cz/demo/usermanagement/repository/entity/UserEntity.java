package cz.demo.usermanagement.repository.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * DB class of User
 *
 * nice to have: roles
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="Users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false, nullable = false)
    private Integer id; // todo OWASP recommends using complex identifiers as part of a defence in depth strategy

    @Column(unique = true)
    @NotBlank
    private String userName;

    @Column
    @NotBlank
    private String password;

    @Column
    private String firstName;

    @Column
    private String lastName;

    @ToString.Include(name = "password")
    public String getPasswordMasked() {
        return password != null ? "***" : "null";
    }

}

