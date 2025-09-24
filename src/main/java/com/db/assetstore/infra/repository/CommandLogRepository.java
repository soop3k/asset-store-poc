package com.db.assetstore.infra.repository;

import com.db.assetstore.infra.jpa.CommandLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommandLogRepository extends JpaRepository<CommandLogEntity, Long> {
}
