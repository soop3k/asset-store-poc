package com.db.assetstore.infra.mapper;

import com.db.assetstore.domain.model.Asset;
import com.db.assetstore.infra.jpa.AssetEntity;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-09-25T20:35:39+0200",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 25 (Oracle Corporation)"
)
@Component
public class AssetMapperImpl implements AssetMapper {

    private final AttributesCollectionMapper attributesCollectionMapper;

    @Autowired
    public AssetMapperImpl(AttributesCollectionMapper attributesCollectionMapper) {

        this.attributesCollectionMapper = attributesCollectionMapper;
    }

    @Override
    public Asset toModel(AssetEntity entity) {
        if ( entity == null ) {
            return null;
        }

        Asset.AssetBuilder asset = Asset.builder();

        asset.attributes( attributesCollectionMapper.fromEntities( entity.getAttributes() ) );
        asset.id( entity.getId() );
        asset.type( entity.getType() );
        asset.createdAt( entity.getCreatedAt() );
        asset.version( entity.getVersion() );
        asset.status( entity.getStatus() );
        asset.subtype( entity.getSubtype() );
        asset.statusEffectiveTime( entity.getStatusEffectiveTime() );
        asset.modifiedAt( entity.getModifiedAt() );
        asset.modifiedBy( entity.getModifiedBy() );
        asset.createdBy( entity.getCreatedBy() );
        asset.notionalAmount( entity.getNotionalAmount() );
        asset.year( entity.getYear() );
        asset.wh( entity.getWh() );
        asset.sourceSystemName( entity.getSourceSystemName() );
        asset.externalReference( entity.getExternalReference() );
        asset.description( entity.getDescription() );
        asset.currency( entity.getCurrency() );

        asset.softDelete( entity.getDeleted() != 0 );

        return asset.build();
    }

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
