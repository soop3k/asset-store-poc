package com.db.assetstore.infra.service;

import com.db.assetstore.domain.service.cmd.AssetCommand;
import com.db.assetstore.domain.service.cmd.CommandResult;
import com.db.assetstore.infra.jpa.CommandLogEntity;
import com.db.assetstore.infra.repository.CommandLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommandLogService {

    private final CommandLogRepository commandLogRepository;
    private final ObjectMapper objectMapper;

    public void record(CommandResult<?> result, AssetCommand<?> command) {
        Objects.requireNonNull(result, "result");
        Objects.requireNonNull(command, "command");

        String commandType = command.commandType();
        String payload;
        try {
            payload = objectMapper.writeValueAsString(command);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialise {} command for asset {}", commandType, result.assetId(), e);
            payload = String.valueOf(command);
        }

        CommandLogEntity entity = CommandLogEntity.builder()
                .commandType(commandType)
                .assetId(result.assetId())
                .payload(payload)
                .createdAt(Instant.now())
                .build();

        commandLogRepository.save(entity);
    }
}
