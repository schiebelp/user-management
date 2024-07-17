package cz.demo.usermanagement.repository.entity;

import cz.demo.usermanagement.repository.enums.ROLE;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.util.Collection;


/**
 * Roles User can possess
 *
 * Nice to have: Privileges for more granular approach, if needed
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"name"})})
public class Role {

    @Id
    @Column(name = "role_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToMany(mappedBy = "roles")
    private Collection<User> users;

    @Enumerated(EnumType.STRING)
    private ROLE name; // e.g., ROLE_ADMIN, ROLE_USER

//    For more granular approach if simple roles are not enough
//    @ManyToMany
//    private Collection<Privilege> privileges;

}
