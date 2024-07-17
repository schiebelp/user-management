package cz.demo.usermanagement.repository.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.Set;

/**
 * DB class of User
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="user_account") // user is preserved keyword in postgres
public class User {

    /**
     * User id
     *
     * OWASP recommends using complex identifiers (UUID) as part of a defence in depth strategy
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", updatable = false, nullable = false)
    private Integer id;

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

    /**
     * Assigned roles
     *
     * Many to many:
     *  User can posess many roles
     *  Role can be assigned to many users
     *
     * Cascade:
     *  PERSIST to persist along with user
     *  MERGE to update along with the user
     *  This way the new role will be persisted and wont be deleted
     */
    @ManyToMany(fetch=FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles;

    @ToString.Include(name = "password")
    public String getPasswordMasked() {
        return password != null ? "***" : "null";
    }

}

