package com.db.assetstore.infra.mapper;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.model.Asset;
import com.db.assetstore.domain.model.attribute.AttributesCollection;
import com.db.assetstore.infra.jpa.AssetEntity;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-09-22T21:41:07+0200",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 25 (Oracle Corporation)"
)
@Component
public class AssetMapperImpl implements AssetMapper {

    @Override
    public AssetEntity toEntity(Asset asset) {
        if ( asset == null ) {
            return null;
        }

        AssetEntity assetEntity = new AssetEntity();

        assetEntity.setId( asset.getId() );
        assetEntity.setVersion( asset.getVersion() );
        assetEntity.setType( asset.getType() );
        assetEntity.setStatus( asset.getStatus() );
        assetEntity.setSubtype( asset.getSubtype() );
        assetEntity.setStatusEffectiveTime( asset.getStatusEffectiveTime() );
        assetEntity.setCreatedAt( asset.getCreatedAt() );
        assetEntity.setCreatedBy( asset.getCreatedBy() );
        assetEntity.setModifiedAt( asset.getModifiedAt() );
        assetEntity.setModifiedBy( asset.getModifiedBy() );
        assetEntity.setNotionalAmount( asset.getNotionalAmount() );
        assetEntity.setYear( asset.getYear() );
        assetEntity.setWh( asset.getWh() );
        assetEntity.setSourceSystemName( asset.getSourceSystemName() );
        assetEntity.setExternalReference( asset.getExternalReference() );
        assetEntity.setDescription( asset.getDescription() );
        assetEntity.setCurrency( asset.getCurrency() );

        fillAttributesEntity( asset, assetEntity );

        return assetEntity;
    }

    @Override
    public Asset toModel(AssetEntity entity) {
        if ( entity == null ) {
            return null;
        }

        String id = null;
        AssetType type = null;
        Instant createdAt = null;

        id = entity.getId();
        type = entity.getType();
        createdAt = entity.getCreatedAt();

        AttributesCollection attributes = null;

        Asset asset = new Asset( id, type, createdAt, attributes );

        asset.setVersion( entity.getVersion() );
        asset.setStatus( entity.getStatus() );
        asset.setSubtype( entity.getSubtype() );
        asset.setStatusEffectiveTime( entity.getStatusEffectiveTime() );
        asset.setModifiedAt( entity.getModifiedAt() );
        asset.setModifiedBy( entity.getModifiedBy() );
        asset.setCreatedBy( entity.getCreatedBy() );
        asset.setNotionalAmount( entity.getNotionalAmount() );
        asset.setYear( entity.getYear() );
        asset.setWh( entity.getWh() );
        asset.setSourceSystemName( entity.getSourceSystemName() );
        asset.setExternalReference( entity.getExternalReference() );
        asset.setDescription( entity.getDescription() );
        asset.setCurrency( entity.getCurrency() );

        fillAttributesModel( entity, asset );

        return asset;
    }

    @Override
    public List<Asset> toModelList(List<AssetEntity> entities) {
        if ( entities == null ) {
            return null;
        }

        List<Asset> list = new ArrayList<Asset>( entities.size() );
        for ( AssetEntity assetEntity : entities ) {
            list.add( toModel( assetEntity ) );
        }

        return list;
    }

    @Override
    public List<AssetEntity> toEntityList(List<Asset> models) {
        if ( models == null ) {
            return null;
        }

        List<AssetEntity> list = new ArrayList<AssetEntity>( models.size() );
        for ( Asset asset : models ) {
            list.add( toEntity( asset ) );
        }

        return list;
    }
}
