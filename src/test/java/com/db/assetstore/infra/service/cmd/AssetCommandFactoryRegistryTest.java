package com.db.assetstore.infra.service.cmd;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.service.cmd.CreateAssetCommand;
import com.db.assetstore.domain.service.cmd.PatchAssetCommand;
import com.db.assetstore.infra.api.dto.AssetCreateRequest;
import com.db.assetstore.infra.api.dto.AssetPatchRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssetCommandFactoryRegistryTest {

    @Mock
    CreateAssetCommandFactory createFactory;

    @Mock
    PatchAssetCommandFactory patchFactory;

    @InjectMocks
    AssetCommandFactoryRegistry registry;

    @Mock
    AssetCreateRequest createRequest;

    @Mock
    AssetPatchRequest patchRequest;

    @Mock
    CreateAssetCommand createCommand;

    @Mock
    PatchAssetCommand patchCommand;

    @Test
    void createCreateCommand_delegatesToCreateFactory() {
        when(createFactory.createCommand(createRequest)).thenReturn(createCommand);

        CreateAssetCommand result = registry.createCreateCommand(createRequest);

        assertThat(result).isSameAs(createCommand);
        verify(createFactory).createCommand(createRequest);
    }

    @Test
    void createPatchCommand_withExplicitId_delegatesToPatchFactory() {
        when(patchFactory.createCommand(AssetType.CRE, "id-1", patchRequest)).thenReturn(patchCommand);

        PatchAssetCommand result = registry.createPatchCommand(AssetType.CRE, "id-1", patchRequest);

        assertThat(result).isSameAs(patchCommand);
        verify(patchFactory).createCommand(AssetType.CRE, "id-1", patchRequest);
    }

    @Test
    void createPatchCommand_usingRequestId_validatesPresence() {
        when(patchRequest.getId()).thenReturn("id-2");
        when(patchFactory.createCommand(eq(AssetType.SHIP), eq("id-2"), any())).thenReturn(patchCommand);

        PatchAssetCommand result = registry.createPatchCommand(AssetType.SHIP, patchRequest);

        assertThat(result).isSameAs(patchCommand);
        verify(patchFactory).createCommand(AssetType.SHIP, "id-2", patchRequest);
    }

    @Test
    void createPatchCommand_withoutRequestId_throwsException() {
        when(patchRequest.getId()).thenReturn(null);

        assertThatThrownBy(() -> registry.createPatchCommand(AssetType.CRE, patchRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("asset id");
    }
}
