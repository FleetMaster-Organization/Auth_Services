package com.services.auth.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
public class Role {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id_role", nullable = false, updatable = false)
    private UUID idRole;

    @Column(name = "name_role", nullable = false, unique = true, length = 45)
    private String nameRole;

    @ManyToMany(mappedBy = "roles")
    private Set<User> users = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Role other)) return false;
        return idRole != null && idRole.equals(other.idRole);
    }

    @Override
    public int hashCode() {
        return idRole != null ? idRole.hashCode() : 0;
    }
}