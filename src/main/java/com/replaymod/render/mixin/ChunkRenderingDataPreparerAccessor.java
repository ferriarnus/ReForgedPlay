package com.replaymod.render.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.client.renderer.SectionOcclusionGraph;
import net.minecraft.client.renderer.ViewArea;

@Mixin(SectionOcclusionGraph.class)
public interface ChunkRenderingDataPreparerAccessor {
    @Accessor("viewArea")
    ViewArea builtChunkStorage();

    @Accessor("needsFullUpdate")
    boolean terrainUpdateScheduled();

    @Accessor("fullUpdateTask")
    Future<?> terrainUpdateFuture();
}