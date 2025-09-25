package com.db.assetstore.domain.service.cmd;

import com.db.assetstore.domain.model.attribute.AttributeValue;
import lombok.Builder;
import org.springframework.lang.NonNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Command representing a request to patch (partially update) an existing asset.
 */
@Builder
public record PatchAssetCommand(
        @NonNull String assetId,
        String status,
        String subtype,
        BigDecimal notionalAmount,
        Integer year,
        String description,
        String currency,
        List<AttributeValue<?>> attributes,
        String executedBy,
        Instant requestTime
) implements AssetCommand<Void> {

    @Override
    public CommandResult<Void> accept(AssetCommandVisitor visitor) {
        return requireVisitor(visitor).visit(this);
    }

}
