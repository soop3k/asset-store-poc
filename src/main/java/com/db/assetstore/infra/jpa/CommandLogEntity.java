package com.db.assetstore.infra.jpa;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "command_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommandLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "command_type", length = 64, nullable = false)
    private String commandType;

    @Column(name = "asset_id", length = 64)
    private String assetId;

    @Lob
    @Column(name = "payload", nullable = false)
    private String payload;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
