package com.db.assetstore.domain.service.cmd;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.exception.command.CommandException;
import com.db.assetstore.domain.model.attribute.AttributeValue;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Application-layer command representing a request to create an Asset.
 * Attributes are already parsed into domain AttributeValue list.
 */
@Builder
public record CreateAssetCommand(
        String id,
        AssetType type,
        String status,
        String subtype,
        BigDecimal notionalAmount,
        Integer year,
        String description,
        String currency,
        List<AttributeValue<?>> attributes,
        String executedBy,
        Instant requestTime
) implements AssetCommand<String> {

    @Override
    public CommandResult<String> accept(AssetCommandVisitor visitor) throws CommandException {
        return requireVisitor(visitor).visit(this);
    }

}
