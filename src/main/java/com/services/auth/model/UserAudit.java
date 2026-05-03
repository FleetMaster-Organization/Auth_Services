package com.services.auth.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "users_audits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_audit")
    private Long idAudit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user", nullable = false)
    private User user;

    @Column(name = "action_type", nullable = false, length = 20)
    private String actionType;

    @Column(name = "modified_field", length = 50)
    private String modifiedField;

    @Column(name = "old_value")
    private String oldValue;

    @Column(name = "new_value")
    private String newValue;

    @Column(name = "modified_by", nullable = false, length = 100)
    private String modifiedBy;

    @Column(name = "modified_at", nullable = false)
    @Builder.Default
    private Instant modifiedAt = Instant.now();
}