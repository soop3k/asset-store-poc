package com.db.assetstore.infra.mapper;

import com.db.assetstore.domain.model.Asset;
import com.db.assetstore.infra.api.dto.AssetCreateRequest;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-09-23T12:25:19+0200",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 25 (Oracle Corporation)"
)
@Component
public class AssetCreateMapperImpl extends AssetCreateMapper {

    @Override
    public Asset toAsset(AssetCreateRequest req) {
        if ( req == null ) {
            return null;
        }

        Asset.AssetBuilder asset = Asset.builder();

        asset.id( generateId( req ) );
        asset.attributes( mapAttributes( req ) );
        asset.type( req.type() );

        asset.createdAt( java.time.Instant.now() );

        return asset.build();
    }
}
