package com.replaymod.render.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;
import net.minecraft.client.renderer.LevelRenderer;

@Mixin(LevelRenderer.class)
public interface WorldRendererAccessor {
    //#if MC<11500
    //$$ @Accessor("field_4076")
    //$$ void setRenderEntitiesStartupCounter(int value);
    //$$
    //#if MC>=10800
    //$$ @Accessor("chunkBuilder")
    //$$ ChunkBuilder getRenderDispatcher();
    //$$
    //$$ @Accessor("needsTerrainUpdate")
    //$$ void setDisplayListEntitiesDirty(boolean value);
    //$$
    //$$ @Accessor("chunksToRebuild")
    //#if MC>=11500
    //$$ Set<BuiltChunk> getChunksToUpdate();
    //#else
    //$$ Set<ChunkRenderer> getChunksToUpdate();
    //#endif
    //#endif
    //#endif
}
