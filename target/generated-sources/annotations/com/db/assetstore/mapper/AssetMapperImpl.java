package com.db.assetstore.mapper;

import com.db.assetstore.infra.jpa.AssetEntity;
import com.db.assetstore.infra.mapper.AssetMapper;
import com.db.assetstore.domain.model.Asset;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-09-21T16:47:08+0200",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 25 (Oracle Corporation)"
)
public class AssetMapperImpl implements AssetMapper {

    @Override
    public AssetEntity toEntity(Asset asset) {
        if ( asset == null ) {
            return null;
        }

        AssetEntity.AssetEntityBuilder assetEntity = AssetEntity.builder();

        assetEntity.id( asset.getId() );
        assetEntity.version( asset.getVersion() );
        assetEntity.type( asset.getType() );
        assetEntity.status( asset.getStatus() );
        assetEntity.subtype( asset.getSubtype() );
        assetEntity.statusEffectiveTime( asset.getStatusEffectiveTime() );
        assetEntity.createdAt( asset.getCreatedAt() );
        assetEntity.createdBy( asset.getCreatedBy() );
        assetEntity.modifiedAt( asset.getModifiedAt() );
        assetEntity.modifiedBy( asset.getModifiedBy() );
        assetEntity.notionalAmount( asset.getNotionalAmount() );
        assetEntity.year( asset.getYear() );
        assetEntity.wh( asset.getWh() );
        assetEntity.sourceSystemName( asset.getSourceSystemName() );
        assetEntity.externalReference( asset.getExternalReference() );
        assetEntity.description( asset.getDescription() );
        assetEntity.currency( asset.getCurrency() );

        assetEntity.deleted( asset.isSoftDelete() ? 1 : 0 );

        return assetEntity.build();
    }

    @Override
    public void updateModelFromEntity(AssetEntity entity, Asset target) {
        if ( entity == null ) {
            return;
        }

        target.setId( entity.getId() );
        target.setType( entity.getType() );
        target.setCreatedAt( entity.getCreatedAt() );
        target.setVersion( entity.getVersion() );
        target.setStatus( entity.getStatus() );
        target.setSubtype( entity.getSubtype() );
        target.setStatusEffectiveTime( entity.getStatusEffectiveTime() );
        target.setModifiedAt( entity.getModifiedAt() );
        target.setModifiedBy( entity.getModifiedBy() );
        target.setCreatedBy( entity.getCreatedBy() );
        target.setNotionalAmount( entity.getNotionalAmount() );
        target.setYear( entity.getYear() );
        target.setWh( entity.getWh() );
        target.setSourceSystemName( entity.getSourceSystemName() );
        target.setExternalReference( entity.getExternalReference() );
        target.setDescription( entity.getDescription() );
        target.setCurrency( entity.getCurrency() );

        target.setSoftDelete( entity.getDeleted() != 0 );
    }
}
