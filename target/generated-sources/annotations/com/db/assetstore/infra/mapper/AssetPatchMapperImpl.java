package com.db.assetstore.infra.mapper;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.model.AssetPatch;
import com.db.assetstore.infra.api.dto.AssetPatchItemRequest;
import com.db.assetstore.infra.api.dto.AssetPatchRequest;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-09-23T12:25:19+0200",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 25 (Oracle Corporation)"
)
@Component
public class AssetPatchMapperImpl extends AssetPatchMapper {

    @Override
    public AssetPatch toPatch(AssetType type, AssetPatchRequest req) {
        if ( type == null && req == null ) {
            return null;
        }

        AssetPatch.AssetPatchBuilder assetPatch = AssetPatch.builder();

        if ( req != null ) {
            assetPatch.status( req.getStatus() );
            assetPatch.subtype( req.getSubtype() );
            assetPatch.notionalAmount( req.getNotionalAmount() );
            assetPatch.year( req.getYear() );
            assetPatch.description( req.getDescription() );
            assetPatch.currency( req.getCurrency() );
        }

        setPatchRequestAttributes( type, req, assetPatch );

        return assetPatch.build();
    }

    @Override
    public AssetPatch toPatch(AssetType type, AssetPatchItemRequest item) {
        if ( type == null && item == null ) {
            return null;
        }

        AssetPatch.AssetPatchBuilder assetPatch = AssetPatch.builder();

        if ( item != null ) {
            assetPatch.status( item.getStatus() );
            assetPatch.subtype( item.getSubtype() );
            assetPatch.notionalAmount( item.getNotionalAmount() );
            assetPatch.year( item.getYear() );
            assetPatch.description( item.getDescription() );
            assetPatch.currency( item.getCurrency() );
        }

        setPatchItemAttributes( type, item, assetPatch );

        return assetPatch.build();
    }
}
