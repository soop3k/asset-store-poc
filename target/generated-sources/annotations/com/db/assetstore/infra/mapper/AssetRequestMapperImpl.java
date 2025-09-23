package com.db.assetstore.infra.mapper;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.service.CreateAssetCommand;
import com.db.assetstore.domain.service.PatchAssetCommand;
import com.db.assetstore.infra.api.dto.AssetCreateRequest;
import com.db.assetstore.infra.api.dto.AssetPatchItemRequest;
import com.db.assetstore.infra.api.dto.AssetPatchRequest;
import java.time.Instant;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-09-23T12:25:19+0200",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 25 (Oracle Corporation)"
)
@Component
public class AssetRequestMapperImpl extends AssetRequestMapper {

    @Override
    public CreateAssetCommand toCreateCommand(AssetCreateRequest req, String createdBy, Instant requestTime) {
        if ( req == null && createdBy == null && requestTime == null ) {
            return null;
        }

        CreateAssetCommand.CreateAssetCommandBuilder createAssetCommand = CreateAssetCommand.builder();

        if ( req != null ) {
            createAssetCommand.id( req.id() );
            createAssetCommand.type( req.type() );
            createAssetCommand.status( req.status() );
            createAssetCommand.subtype( req.subtype() );
            createAssetCommand.notionalAmount( req.notionalAmount() );
            createAssetCommand.year( req.year() );
            createAssetCommand.description( req.description() );
            createAssetCommand.currency( req.currency() );
        }
        createAssetCommand.createdBy( createdBy );
        createAssetCommand.requestTime( requestTime != null ? requestTime : java.time.Instant.now() );

        setCreateAttributes( req, createAssetCommand );

        return createAssetCommand.build();
    }

    @Override
    public PatchAssetCommand toPatchCommand(AssetType type, String id, AssetPatchRequest req, String modifiedBy, Instant requestTime) {
        if ( type == null && id == null && req == null && modifiedBy == null && requestTime == null ) {
            return null;
        }

        PatchAssetCommand.PatchAssetCommandBuilder patchAssetCommand = PatchAssetCommand.builder();

        if ( req != null ) {
            patchAssetCommand.status( req.getStatus() );
            patchAssetCommand.subtype( req.getSubtype() );
            patchAssetCommand.notionalAmount( req.getNotionalAmount() );
            patchAssetCommand.year( req.getYear() );
            patchAssetCommand.description( req.getDescription() );
            patchAssetCommand.currency( req.getCurrency() );
        }
        patchAssetCommand.modifiedBy( modifiedBy );
        patchAssetCommand.assetId( new com.db.assetstore.domain.model.AssetId(id) );
        patchAssetCommand.requestTime( requestTime != null ? requestTime : java.time.Instant.now() );

        setPatchAttributes( type, req, patchAssetCommand );

        return patchAssetCommand.build();
    }

    @Override
    public PatchAssetCommand toPatchCommand(AssetType type, AssetPatchItemRequest item, String modifiedBy, Instant requestTime) {
        if ( type == null && item == null && modifiedBy == null && requestTime == null ) {
            return null;
        }

        PatchAssetCommand.PatchAssetCommandBuilder patchAssetCommand = PatchAssetCommand.builder();

        if ( item != null ) {
            patchAssetCommand.status( item.getStatus() );
            patchAssetCommand.subtype( item.getSubtype() );
            patchAssetCommand.notionalAmount( item.getNotionalAmount() );
            patchAssetCommand.year( item.getYear() );
            patchAssetCommand.description( item.getDescription() );
            patchAssetCommand.currency( item.getCurrency() );
        }
        patchAssetCommand.modifiedBy( modifiedBy );
        patchAssetCommand.assetId( new com.db.assetstore.domain.model.AssetId(item.getId()) );
        patchAssetCommand.requestTime( requestTime != null ? requestTime : java.time.Instant.now() );

        setPatchItemAttributes( type, item, patchAssetCommand );

        return patchAssetCommand.build();
    }
}
