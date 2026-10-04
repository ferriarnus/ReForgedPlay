package com.replaymod.render.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import com.replaymod.render.hooks.ForceChunkLoadingHook;
import com.replaymod.render.hooks.IForceChunkLoading;
import com.replaymod.render.utils.EmbeddiumFlawlessFramesHelper;
import com.replaymod.render.utils.SodiumFlawlessFramesHelper;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SectionOcclusionGraph;
import net.minecraft.client.renderer.chunk.RenderRegionCache;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;

@Mixin(LevelRenderer.class)
public abstract class Mixin_ForceChunkLoading implements IForceChunkLoading {
    private ForceChunkLoadingHook replayModRender_hook;

    //#if MC>=12109
    private static final String SETUP_TERRAIN = "Lnet/minecraft/client/render/WorldRenderer;method_74752(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/Frustum;Z)V";
    //#else
    //$$ private static final String SETUP_TERRAIN = "Lnet/minecraft/client/render/WorldRenderer;setupTerrain(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/Frustum;ZZ)V";
    //#endif


    @Override
    public void replayModRender_setHook(ForceChunkLoadingHook hook) {
        this.replayModRender_hook = hook;
    }

    @Shadow private SectionRenderDispatcher sectionRenderDispatcher;

    @Shadow @Final private SectionOcclusionGraph sectionOcclusionGraph;

    @Shadow protected abstract void cullTerrain(Camera par1, Frustum par2, boolean par3);

    //@Shadow private Frustum cullingFrustum;

    //@Shadow private Frustum capturedFrustum;

    @Shadow @Final private Minecraft minecraft;

    @Shadow protected abstract void applyFrustum(Frustum par1);

    @WrapMethod(method = "update")
    private void forceAllChunks(Camera camera, Operation<Void> original){
        if (replayModRender_hook == null) {
            original.call(camera);
            return;
        }

        if (EmbeddiumFlawlessFramesHelper.hasEmbeddium() && EmbeddiumFlawlessFramesHelper.supportFlawlessFrames()) {
            original.call(camera);
            return;
        }

        if (SodiumFlawlessFramesHelper.hasSodium() && SodiumFlawlessFramesHelper.supportFlawlessFrames()) {
            original.call(camera);
            return;
        }

        assert this.minecraft.player != null;

        SectionOcclusionGraph renderingData = this.sectionOcclusionGraph;
        ChunkRenderingDataPreparerAccessor renderingDataAcc = (ChunkRenderingDataPreparerAccessor) renderingData;
        RenderRegionCache chunkRendererRegionBuilder = new RenderRegionCache();

        do {
            boolean areWeDoneYet = true;

            // Determine which chunks shall be visible
            original.call(camera);

            // Wait for async processing to be complete
            Future<?> fullUpdateFuture = renderingDataAcc.terrainUpdateFuture();
            if (fullUpdateFuture != null) {
                try {
                    fullUpdateFuture.get(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                } catch (TimeoutException e) {
                    e.printStackTrace();
                }
            }

            //#if MC < 26.1
            // If that async processing did change the chunk graph, we need to re-apply the frustum (otherwise this is
            // only done in the next setupTerrain call, which not happen this frame)
            //$$ if (renderingData.consumeFrustumUpdate()) {
            //$$     this.applyFrustum((new Frustum(cullingFrustum)).offsetToFullyIncludeCameraCube(8)); // call based on the one in setupTerrain
            //$$ }
            //#endif

            // Schedule all chunks which need rebuilding (we schedule even important rebuilds because we wait for
            // all of them anyway and this way we can take advantage of threading)
            for (SectionRenderDispatcher.RenderSection builtChunk : renderingDataAcc.builtChunkStorage().sections) {
                if (!builtChunk.isDirty()) {
                    continue;
                }
                // MC sometimes schedules invalid chunks when you're outside of loaded chunks (e.g. y > 256)
                if (builtChunk.hasAllNeighbors()) {
                    //#if MC>=12106
                    builtChunk.rebuildSectionAsync(chunkRendererRegionBuilder);
                    //#else
                    //$$ builtChunk.scheduleRebuild(this.field_45614, chunkRendererRegionBuilder);
                    //#endif
                    areWeDoneYet = false;
                }
                builtChunk.setNotDirty();
            }

            // Upload all chunks
            if (((ForceChunkLoadingHook.IBlockOnChunkRebuilds) this.sectionRenderDispatcher).uploadEverythingBlocking()) {
                areWeDoneYet = false;
            }

            // Repeat until no more updates are needed
            if (!areWeDoneYet) {
                renderingData.invalidate(); // sets shouldUpdate to true
            }
        } while (renderingDataAcc.terrainUpdateScheduled());
    }
}