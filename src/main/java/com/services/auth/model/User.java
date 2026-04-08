package com.services.auth.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(generator = "uuid2")
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "id_user", columnDefinition = "BINARY(16)", nullable = false,
    updatable = false)
    private UUID idUser;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "users_roles",
            joinColumns = @JoinColumn(name = "id_user", referencedColumnName = "id_user"),
            inverseJoinColumns = @JoinColumn(name = "id_role", referencedColumnName = "id_role")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @Override
    public boolean equals(Object o){
        if (this ==  o) return true;
        if (!(o instanceof User other)) return false;
        return idUser != null && idUser.equals(other.idUser);
    }

    @Override
    public int hashCode() {
        return idUser != null ? idUser.hashCode() : 0;
    }
}