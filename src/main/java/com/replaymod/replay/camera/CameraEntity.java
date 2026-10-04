package com.replaymod.replay.camera;

import com.replaymod.core.KeyBindingRegistry;
import com.replaymod.core.ReplayMod;
import com.replaymod.core.SettingsRegistry;
import com.replaymod.core.events.KeyBindingEventCallback;
import com.replaymod.core.events.PreRenderCallback;
import com.replaymod.core.events.PreRenderHandCallback;
import com.replaymod.core.events.SettingsChangedCallback;
import com.replaymod.replay.ReplayHandler;
import com.replaymod.replay.events.RenderHotbarCallback;
import com.replaymod.replay.events.RenderSpectatorCrosshairCallback;
import com.replaymod.replay.mixin.EntityPlayerAccessor;
import de.johni0702.minecraft.gui.utils.EventRegistrations;
import de.johni0702.minecraft.gui.versions.callbacks.PreTickCallback;
import com.replaymod.core.utils.Utils;
import com.replaymod.replay.ReplayModReplay;
import com.replaymod.replay.Setting;
import com.replaymod.replay.mixin.FirstPersonRendererAccessor;
import com.replaymod.replaystudio.util.Location;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.chat.ChatAbilities;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.stats.StatsCounter;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import static com.replaymod.core.versions.MCVer.*;

/**
 * The camera entity used as the main player entity during replay viewing.
 * During a replay the player should be an instance of this class.
 * Camera movement is controlled by a separate {@link CameraController}.
 */
@SuppressWarnings("EntityConstructor")
public class CameraEntity
        //#if MC>=10800
        extends LocalPlayer
        //#else
        //$$ extends EntityClientPlayerMP
        //#endif
{
    private static final UUID CAMERA_UUID = UUID.nameUUIDFromBytes("ReplayModCamera".getBytes(StandardCharsets.UTF_8));

    /**
     * Roll of this camera in degrees.
     */
    public float roll;

    private CameraController cameraController;

    private long lastControllerUpdate = System.currentTimeMillis();

    /**
     * The entity whose hand was the last one rendered.
     */
    private Entity lastHandRendered = null;

    /**
     * The hashCode and equals methods of Entity are not stable.
     * Therefore we cannot register any event handlers directly in the CameraEntity class and
     * instead have this inner class.
     */
    private EventHandler eventHandler = new EventHandler();

    public CameraEntity(
            Minecraft mcIn,
            //#if MC>=11400
            ClientLevel worldIn,
            //#else
            //$$ World worldIn,
            //#endif
            //#if MC<10800
            //$$ Session session,
            //#endif
            ClientPacketListener netHandlerPlayClient,
            StatsCounter statisticsManager
            //#if MC>=11200
            //#if MC>=11400
            , ClientRecipeBook recipeBook
            //#else
            //$$ , RecipeBook recipeBook
            //#endif
            //#endif
    ) {
        super(mcIn,
                worldIn,
                //#if MC<10800
                //$$ session,
                //#endif
                netHandlerPlayClient,
                statisticsManager
                //#if MC>=11200
                , recipeBook
                //#endif
                //#if MC>=11600
                //#if MC>=12106
                 , Input.EMPTY
                //#else
                //$$, false
                //#endif
                , false
                //#endif
                , ChatAbilities.NO_RESTRICTIONS
        );
        //#if MC>=10900
        setUUID(CAMERA_UUID);
        //#else
        //$$ entityUniqueID = CAMERA_UUID;
        //#endif
        eventHandler.register();
        if (ReplayModReplay.instance.getReplayHandler().getSpectatedUUID() == null) {
            cameraController = ReplayModReplay.instance.createCameraController(this);
        } else {
            cameraController = new SpectatorCameraController(this);
        }
    }

    public CameraController getCameraController() {
        return cameraController;
    }

    public void setCameraController(CameraController cameraController) {
        this.cameraController = cameraController;
    }

    /**
     * Moves the camera by the specified delta.
     * @param x Delta in X direction
     * @param y Delta in Y direction
     * @param z Delta in Z direction
     */
    public void moveCamera(double x, double y, double z) {
        setCameraPosition(this.getX() + x, this.getY() + y, this.getZ() + z);
    }

    /**
     * Set the camera position.
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     */
    public void setCameraPosition(double x, double y, double z) {
        this.xOld = this.xo = x;
        this.yOld = this.yo = y;
        this.zOld = this.zo = z;
        this.setPosRaw(x, y, z);
        updateBoundingBox();
    }

    /**
     * Sets the camera rotation.
     * @param yaw Yaw in degrees
     * @param pitch Pitch in degrees
     * @param roll Roll in degrees
     */
    public void setCameraRotation(float yaw, float pitch, float roll) {
        //#if MC>=12102
        // Note: MC's `setPitch` now forces values into the [-90; 90] range, however the math it uses is incorrect, so
        //       we need to wrap our value into the [-180; 180] range first to get correct results.
        pitch = Mth.wrapDegrees(pitch);
        //#endif
        this.yRotO = yaw;
        this.xRotO = pitch;
        this.setYRot(yaw);
        this.setXRot(pitch);
        this.roll = roll;
    }

    /**
     * Sets the camera position and rotation to that of the specified AdvancedPosition
     * @param pos The position and rotation to set
     */
    public void setCameraPosRot(Location pos) {
        setCameraRotation(pos.getYaw(), pos.getPitch(), roll);
        setCameraPosition(pos.getX(), pos.getY(), pos.getZ());
    }

    /**
     * Sets the camera position and rotation to that of the specified entity.
     * @param to The entity whose position to copy
     */
    public void setCameraPosRot(Entity to) {
        if (to == this) return;
        //#if MC>=10800
        float yOffset = 0;
        //#else
        //$$ float yOffset = 1.62f; // Magic value (eye height) from EntityRenderer#orientCamera
        //#endif
        this.xo = to.xo;
        this.yo = to.yo + yOffset;
        this.zo = to.zo;
        this.yRotO = to.yRotO;
        this.xRotO = to.xRotO;
        this.setPosRaw(to.getX(), to.getY(), to.getZ());
        this.setYRot(to.getYRot());
        this.setXRot(to.getXRot());
        this.xOld = to.xOld;
        this.yOld = to.yOld + yOffset;
        this.zOld = to.zOld;
        this.wrapArmYaw();
        updateBoundingBox();
    }

    //#if MC>=11400
    @Override
    public float getViewYRot(float tickDelta) {
        Entity view = this.minecraft.getCameraEntity();
        if (view != null && view != this) {
            return this.yRotO + (this.getYRot() - this.yRotO) * tickDelta;
        }
        return super.getViewYRot(tickDelta);
    }

    @Override
    public float getViewXRot(float tickDelta) {
        Entity view = this.minecraft.getCameraEntity();
        if (view != null && view != this) {
            return this.xRotO + (this.getXRot() - this.xRotO) * tickDelta;
        }
        return super.getViewXRot(tickDelta);
    }
    //#endif

    private void updateBoundingBox() {
        //#if MC>=11400
        float width = getBbWidth();
        float height = getBbHeight();
        //#endif
        //#if MC>=10800
        setBoundingBox(new AABB(
        //#else
        //$$ this.boundingBox.setBB(AxisAlignedBB.getBoundingBox(
        //#endif
                this.getX() - width / 2, this.getY(), this.getZ() - width / 2,
                this.getX() + width / 2, this.getY() + height, this.getZ() + width / 2));
    }

    @Override
    public void tick() {
        //#if MC>=10800
        Entity view =
        //#else
        //$$ EntityLivingBase view =
        //#endif
            this.minecraft.getCameraEntity();
        if (view != null) {
            // Make sure we're always spectating the right entity
            // This is important if the spectated player respawns as their
            // entity is recreated and we have to spectate a new entity
            UUID spectating = ReplayModReplay.instance.getReplayHandler().getSpectatedUUID();
            if (spectating != null && (view.getUUID() != spectating
                    || view.level() != this.level())
                    || this.level().getEntity(view.getId()) != view) {
                if (spectating == null) {
                    // Entity (non-player) died, stop spectating
                    ReplayModReplay.instance.getReplayHandler().spectateEntity(this);
                    return;
                }
                view = this.level().getPlayerByUUID(spectating);
                if (view != null) {
                    this.minecraft.setCameraEntity(view);
                } else {
                    this.minecraft.setCameraEntity(this);
                    return;
                }
            }
            // Move cmera to their position so when we exit the first person view
            // we don't jump back to where we entered it
            if (view != this) {
                setCameraPosRot(view);
            }
        }
    }

    @Override
    public void resetPos() {
        // Make sure our world is up-to-date in case of world changes
        if (this.minecraft.level != null) {
            // FIXME cannot use Patters because `setWorld` is `protected` in 1.20
            //#if MC>=12000
            this.setLevel(this.minecraft.level);
            //#else
            //$$ this.world = this.client.world;
            //#endif
        }
        super.resetPos();
    }

    @Override
    public void setRot(float yaw, float pitch) {
        if (this.minecraft.getCameraEntity() == this) {
            // Only update camera rotation when the camera is the view
            super.setRot(yaw, pitch);
        }
    }

    @Override
    public boolean isInWall() {
        return falseUnlessSpectating(Entity::isInWall); // Make sure no suffocation overlay is rendered
    }

    //#if MC<11400
    //$$ @Override
    //$$ public boolean isInsideOfMaterial(Material materialIn) {
    //$$     return falseUnlessSpectating(e -> e.isInsideOfMaterial(materialIn)); // Make sure no overlays are rendered
    //$$ }
    //#endif

    //#if MC>=11400
    @Override
    public boolean isEyeInFluid(
            //#if MC>=11802
            TagKey<Fluid> fluid
            //#else
            //$$ Tag<Fluid> fluid
            //#endif
    ) {
        return falseUnlessSpectating(entity -> entity.isEyeInFluid(fluid));
    }

    @Override
    public float getWaterVision() {
        return falseUnlessSpectating(__ -> true) ? super.getWaterVision() : 1f;
    }
    //#else
    //#if MC>=10800
    //$$ @Override
    //$$ public boolean isInLava() {
    //$$     return falseUnlessSpectating(Entity::isInLava); // Make sure no lava overlay is rendered
    //$$ }
    //#else
    //$$ @Override
    //$$ public boolean handleLavaMovement() {
    //$$     return falseUnlessSpectating(Entity::handleLavaMovement); // Make sure no lava overlay is rendered
    //$$ }
    //#endif
    //$$
    //$$ @Override
    //$$ public boolean isInWater() {
    //$$     return falseUnlessSpectating(Entity::isInWater); // Make sure no water overlay is rendered
    //$$ }
    //#endif

    @Override
    public boolean isOnFire() {
        return falseUnlessSpectating(Entity::isOnFire); // Make sure no fire overlay is rendered
    }

    private boolean falseUnlessSpectating(Function<Entity, Boolean> property) {
        Entity view = this.minecraft.getCameraEntity();
        if (view != null && view != this) {
            return property.apply(view);
        }
        return false;
    }

    @Override
    public boolean isPushable() {
        return false; // We are in full control of ourselves
    }

    //#if MC>=10800
    @Override
    protected void spawnSprintParticle() {
        // We do not produce any particles, we are a camera
    }
    //#endif

    @Override
    public boolean isPickable() {
        return false; // We are a camera, we cannot collide
    }

    //#if MC>=10800
    @Override
    public boolean isSpectator() {
        ReplayHandler replayHandler = ReplayModReplay.instance.getReplayHandler();
        return replayHandler == null || replayHandler.isCameraView(); // Make sure we're treated as spectator
    }
    //#endif

    //#if MC>=11400
    @Override
    public boolean shouldRender(double double_1, double double_2, double double_3) {
        return false; // never render the camera otherwise it'd be visible e.g. in 3rd-person or with shaders
    }
    //#else
    //$$ @Override
    //$$ public boolean shouldRenderInPass(int pass) {
    //$$     // Never render the camera
    //$$     // This is necessary to hide the player head in third person mode and to not
    //$$     // cause any unwanted shadows when rendering with shaders.
    //$$     return false;
    //$$ }
    //#endif

    //#if MC>=12102
    @Override
    public float getFieldOfViewModifier(boolean firstPerson, float fovEffectScale) {
        Entity view = this.minecraft.getCameraEntity();
        if (view != this && view instanceof AbstractClientPlayer) {
            return ((AbstractClientPlayer) view).getFieldOfViewModifier(firstPerson, fovEffectScale);
        }
        return 1;
    }
    //#elseif MC>=10800
    //$$ @Override
    //$$ public float getFovMultiplier() {
    //$$     Entity view = this.client.getCameraEntity();
    //$$     if (view != this && view instanceof AbstractClientPlayerEntity) {
    //$$         return ((AbstractClientPlayerEntity) view).getFovMultiplier();
    //$$     }
    //$$     return 1;
    //$$ }
    //#else
    //$$ @Override
    //$$ public float getFOVMultiplier() {
    //$$     return 1;
    //$$ }
    //#endif

    @Override
    public boolean isInvisible() {
        Entity view = this.minecraft.getCameraEntity();
        if (view != this) {
            return view.isInvisible();
        }
        return super.isInvisible();
    }

    //#if MC>=12002
    @Override
    public PlayerSkin getSkin() {
        Entity view = this.minecraft.getCameraEntity();
        if (view != this && view instanceof AbstractClientPlayer) {
            return ((AbstractClientPlayer) view).getSkin();
        }
        return super.getSkin();
    }
    //#else
    //$$ @Override
    //$$ public Identifier getSkinTexture() {
    //$$     Entity view = this.client.getCameraEntity();
    //$$     if (view != this && view instanceof AbstractClientPlayerEntity) {
    //$$         return ((AbstractClientPlayerEntity) view).getSkinTexture();
    //$$     }
    //$$  return super.getSkinTexture();
    //$$  }

    //#if MC>=10800
    //$$ @Override
    //$$ public String getModel() {
    //$$     Entity view = this.client.getCameraEntity();
    //$$     if (view != this && view instanceof AbstractClientPlayerEntity) {
    //$$         return ((AbstractClientPlayerEntity) view).getModel();
    //$$     }
    //$$     return super.getModel();
    //$$ }
    //#endif
    //#endif

    //#if MC>=10800
    @Override
    public boolean isModelPartShown(PlayerModelPart modelPart) {
        Entity view = this.minecraft.getCameraEntity();
        if (view != this && view instanceof Player) {
            return ((Player) view).isModelPartShown(modelPart);
        }
        return super.isModelPartShown(modelPart);
    }
    //#endif

    //#if MC>=10904
    @Override
    public HumanoidArm getMainArm() {
        Entity view = this.minecraft.getCameraEntity();
        if (view != this && view instanceof Player) {
            return ((Player) view).getMainArm();
        }
        return super.getMainArm();
    }
    //#endif

    @Override
    public float getAttackAnim(float renderPartialTicks) {
        Entity view = this.minecraft.getCameraEntity();
        if (view != this && view instanceof Player) {
            return ((Player) view).getAttackAnim(renderPartialTicks);
        }
        return 0;
    }

    //#if MC>=10904
    @Override
    public float getCurrentItemAttackStrengthDelay() {
        Entity view = this.minecraft.getCameraEntity();
        if (view != this && view instanceof Player) {
            return ((Player) view).getCurrentItemAttackStrengthDelay();
        }
        return 1;
    }

    @Override
    public float getAttackStrengthScale(float adjustTicks) {
        Entity view = this.minecraft.getCameraEntity();
        if (view != this && view instanceof Player) {
            return ((Player) view).getAttackStrengthScale(adjustTicks);
        }
        // Default to 1 as to not render the cooldown indicator (renders for < 1)
        return 1;
    }

    @Override
    public InteractionHand getUsedItemHand() {
        Entity view = this.minecraft.getCameraEntity();
        if (view != this && view instanceof Player) {
            return ((Player) view).getUsedItemHand();
        }
        return super.getUsedItemHand();
    }

    @Override
    public boolean isUsingItem() {
        Entity view = this.minecraft.getCameraEntity();
        if (view != this && view instanceof Player) {
            return ((Player) view).isUsingItem();
        }
        return super.isUsingItem();
    }

    //#if MC>=11400
    @Override
    //#if MC>=11900
    public void onEquipItem(EquipmentSlot slot, ItemStack stack, ItemStack itemStack) {
    //#else
    //$$ protected void onEquipStack(ItemStack itemStack_1) {
    //#endif
        // Suppress equip sounds
    }
    //#endif

    //#if MC>=11400
    @Override
    public HitResult pick(double maxDistance, float tickDelta, boolean fluids) {
        HitResult result = super.pick(maxDistance, tickDelta, fluids);

        // Make sure we can never look at blocks (-> no outline)
        if (result instanceof BlockHitResult) {
            BlockHitResult blockResult = (BlockHitResult) result;
            result = BlockHitResult.miss(result.getLocation(), blockResult.getDirection(), blockResult.getBlockPos());
        }

        return result;
    }
    //#else
    //#if MC>=11400
    //$$ @Override
    //$$ public RayTraceResult rayTrace(double blockReachDistance, float partialTicks, RayTraceFluidMode p_174822_4_) {
    //$$     RayTraceResult pos = super.rayTrace(blockReachDistance, partialTicks, p_174822_4_);
    //$$
    //$$     // Make sure we can never look at blocks (-> no outline)
    //$$     if(pos != null && pos.type == RayTraceResult.Type.BLOCK) {
    //$$         pos.type = RayTraceResult.Type.MISS;
    //$$     }
    //$$
    //$$     return pos;
    //$$ }
    //#else
    //$$ @Override
    //$$ public RayTraceResult rayTrace(double p_174822_1_, float p_174822_3_) {
    //$$     RayTraceResult pos = super.rayTrace(p_174822_1_, 1f);
    //$$
    //$$     // Make sure we can never look at blocks (-> no outline)
    //$$     if(pos != null && pos.typeOfHit == RayTraceResult.Type.BLOCK) {
    //$$         pos.typeOfHit = RayTraceResult.Type.MISS;
    //$$     }
    //$$
    //$$     return pos;
    //$$ }
    //#endif
    //#endif
    //#else
    //$$ @Override
    //$$ public MovingObjectPosition rayTrace(double p_174822_1_, float p_174822_3_) {
    //$$     MovingObjectPosition pos = super.rayTrace(p_174822_1_, 1f);
    //$$
    //$$     // Make sure we can never look at blocks (-> no outline)
    //$$     if(pos != null && pos.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
    //$$         pos.typeOfHit = MovingObjectPosition.MovingObjectType.MISS;
    //$$     }
    //$$
    //$$     return pos;
    //$$ }
    //#endif

    //#if MC<11400
    //$$ @Override
    //$$ public void openGui(Object mod, int modGuiId, World world, int x, int y, int z) {
    //$$     // Do not open any block GUIs for the camera entities
    //$$     // Note: Vanilla GUIs are filtered out on a packet level, this only applies to mod GUIs
    //$$ }
    //#endif

    @Override
    //#if MC>=11700
    public void remove(RemovalReason reason) {
        super.remove(reason);
    //#else
    //$$ public void remove() {
    //$$     super.remove();
    //#endif
        if (eventHandler != null) {
            eventHandler.unregister();
            eventHandler = null;
        }
    }

    private void update() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != this.level()) {
            if (eventHandler != null) {
                eventHandler.unregister();
                eventHandler = null;
            }
            return;
        }

        long now = System.currentTimeMillis();
        long timePassed = now - lastControllerUpdate;
        cameraController.update(timePassed / 50f);
        lastControllerUpdate = now;

        handleInputEvents();

        Map<String, KeyBindingRegistry.Binding> keyBindings = ReplayMod.instance.getKeyBindingRegistry().getBindings();
        if (keyBindings.get("replaymod.input.rollclockwise").keyBinding.isDown()) {
            roll += Utils.isCtrlDown() ? 0.2 : 1;
        }
        if (keyBindings.get("replaymod.input.rollcounterclockwise").keyBinding.isDown()) {
            roll -= Utils.isCtrlDown() ? 0.2 : 1;
        }

        //#if MC>=10800
        this.noPhysics = this.isSpectator();
        //#endif

        syncInventory();
    }

    private final Inventory originalInventory = this.getInventory();

    // If we are spectating a player, "steal" its inventory so the rendering code knows what item(s) to render
    // and if we aren't, then reset ours.
    private void syncInventory() {
        Entity view = this.minecraft.getCameraEntity();
        Player viewPlayer = view != this && view instanceof Player ? (Player) view : null;
        EntityPlayerAccessor cameraA = (EntityPlayerAccessor) this;
        EntityPlayerAccessor viewPlayerA = (EntityPlayerAccessor) viewPlayer;

        //#if MC>=11100
        ItemStack empty = ItemStack.EMPTY;
        //#else
        //$$ ItemStack empty = null;
        //#endif

        // TODO switch to replacing the entire inventory for 1.14+ as well, should be easier and faster
        //#if MC>=11400
        this.setItemSlot(EquipmentSlot.HEAD, viewPlayer != null ? viewPlayer.getItemBySlot(EquipmentSlot.HEAD) : empty);
        this.setItemSlot(EquipmentSlot.MAINHAND, viewPlayer != null ? viewPlayer.getItemBySlot(EquipmentSlot.MAINHAND) : empty);
        this.setItemSlot(EquipmentSlot.OFFHAND, viewPlayer != null ? viewPlayer.getItemBySlot(EquipmentSlot.OFFHAND) : empty);
        //#else
        //$$ this.inventory = viewPlayer != null ? viewPlayer.inventory : originalInventory;
        //#endif

        //#if MC>=10904
        cameraA.setItemStackMainHand(viewPlayerA != null ? viewPlayerA.getItemStackMainHand() : empty);
        this.swingingArm = viewPlayer != null ? viewPlayer.swingingArm : InteractionHand.MAIN_HAND;
        this.useItem = viewPlayer != null ? viewPlayer.getUseItem() : empty;
        cameraA.setActiveItemStackUseCount(viewPlayerA != null ? viewPlayerA.getActiveItemStackUseCount() : 0);
        //#else
        //$$ cameraA.setItemInUse(viewPlayerA != null ? viewPlayerA.getItemInUse() : empty);
        //$$ cameraA.setItemInUseCount(viewPlayerA != null ? viewPlayerA.getItemInUseCount() : 0);
        //#endif
    }

    private void handleInputEvents() {
        if (this.minecraft.options.keyAttack.consumeClick() || this.minecraft.options.keyUse.consumeClick()) {
            if (this.minecraft.screen == null && canSpectate(this.minecraft.crosshairPickEntity)) {
                ReplayModReplay.instance.getReplayHandler().spectateEntity(
                        //#if MC<=10710
                        //$$ (EntityLivingBase)
                        //#endif
                        this.minecraft.crosshairPickEntity);
                // Make sure we don't exit right away
                //noinspection StatementWithEmptyBody
                while (this.minecraft.options.keyShift.consumeClick());
            }
        }
    }

    private void updateArmYawAndPitch() {
        this.yBobO = this.yBob;
        this.xBobO = this.xBob;
        this.xBob = this.xBob +  (this.getXRot() - this.xBob) * 0.5f;
        this.yBob = this.yBob + wrapDegrees(this.getYRot() - this.yBob) * 0.5f;
        this.wrapArmYaw();
    }

    /**
     * Minecraft renders the arm offset based on the difference between {@link #yaw} and {@link #yBob}. It does not
     * wrap around the difference though, so if {@link #yaw} just wrapped around from 350 to 10 but {@link #yBob}
     * is still at 355, then the difference will be inappropriately large. To fix this, we always wrap the
     * {@link #yBob} such that it is no more than 180 degrees away from {@link #yaw}, even if that requires going
     * outside the normal range.
     */
    private void wrapArmYaw() {
        this.yBob = wrapDegreesTo(this.yBob, this.getYRot());
        this.yBobO = wrapDegreesTo(this.yBobO, this.yBob);
    }

    private static float wrapDegreesTo(float value, float towardsValue) {
        while (towardsValue - value < -180) {
            value -= 360;
        }
        while (towardsValue - value >= 180) {
            value += 360;
        }
        return value;
    }

    private static float wrapDegrees(float value) {
        value %= 360;
        return wrapDegreesTo(value, 0);
    }

    public boolean canSpectate(Entity e) {
        return e != null
                //#if MC<10800
                //$$ && e instanceof EntityPlayer // cannot be more generic since 1.7.10 has no concept of eye height
                //#endif
                && !e.isInvisible();
    }

    //#if MC<11400
    //#if MC>=11102
    //$$ @Override
    //$$ public void sendMessage(ITextComponent message) {
    //$$     if (MinecraftForge.EVENT_BUS.post(new ReplayChatMessageEvent(this))) return;
    //$$     super.sendMessage(message);
    //$$ }
    //#else
    //$$ @Override
    //$$ public void addChatMessage(ITextComponent message) {
    //$$     if (MinecraftForge.EVENT_BUS.post(new ReplayChatMessageEvent(this))) return;
    //$$     super.addChatMessage(message);
    //$$ }
    //#endif
    //#endif

    //#if MC>=10800
    private
    //#else
    //$$ public // All event handlers need to be public in 1.7.10
    //#endif
    class EventHandler extends EventRegistrations {
        private final Minecraft mc = getMinecraft();

        private EventHandler() {}

        { on(PreTickCallback.EVENT, this::onPreClientTick); }
        private void onPreClientTick() {
            updateArmYawAndPitch();
        }

        { on(PreRenderCallback.EVENT, this::onRenderUpdate); }
        private void onRenderUpdate() {
            update();
        }

        { on(KeyBindingEventCallback.EVENT, CameraEntity.this::handleInputEvents); }

        { on(RenderSpectatorCrosshairCallback.EVENT, this::shouldRenderSpectatorCrosshair); }
        private Boolean shouldRenderSpectatorCrosshair() {
            return canSpectate(mc.crosshairPickEntity);
        }

        { on(RenderHotbarCallback.EVENT, this::shouldRenderHotbar); }
        private Boolean shouldRenderHotbar() {
            return false;
        }

        { on(SettingsChangedCallback.EVENT, this::onSettingsChanged); }
        private void onSettingsChanged(SettingsRegistry registry, SettingsRegistry.SettingKey<?> key) {
            if (key == Setting.CAMERA) {
                if (ReplayModReplay.instance.getReplayHandler().getSpectatedUUID() == null) {
                    cameraController = ReplayModReplay.instance.createCameraController(CameraEntity.this);
                } else {
                    cameraController = new SpectatorCameraController(CameraEntity.this);
                }
            }
        }

        { on(PreRenderHandCallback.EVENT, this::onRenderHand); }
        private boolean onRenderHand() {
            // Unless we are spectating another player, don't render our hand
            Entity view = mc.getCameraEntity();
            if (view == CameraEntity.this || !(view instanceof Player)) {
                return true; // cancel hand rendering
            } else {
                Player player = (Player) view;
                // When the spectated player has changed, force equip their items to prevent the equip animation
                if (lastHandRendered != player) {
                    lastHandRendered = player;

                    FirstPersonRendererAccessor acc = (FirstPersonRendererAccessor) mc.gameRenderer.itemInHandRenderer;
                    //#if MC>=10904
                    acc.setPrevEquippedProgressMainHand(1);
                    acc.setPrevEquippedProgressOffHand(1);
                    acc.setEquippedProgressMainHand(1);
                    acc.setEquippedProgressOffHand(1);
                    acc.setItemStackMainHand(player.getItemBySlot(EquipmentSlot.MAINHAND));
                    acc.setItemStackOffHand(player.getItemBySlot(EquipmentSlot.OFFHAND));
                    //#else
                    //$$ acc.setPrevEquippedProgress(1);
                    //$$ acc.setEquippedProgress(1);
                    //$$ acc.setItemToRender(player.inventory.getCurrentItem());
                    //$$ acc.setEquippedItemSlot(player.inventory.currentItem);
                    //#endif


                    mc.player.yBob = mc.player.yBobO = player.getYRot();
                    mc.player.xBob = mc.player.xBobO = player.getXRot();
                }
                return false;
            }
        }

        //#if MC>=11400
        // Moved to MixinCamera
        //#else
        //#if MC>=10800
        //$$ @SubscribeEvent
        //$$ public void onEntityViewRenderEvent(EntityViewRenderEvent.CameraSetup event) {
        //$$     if (mc.getRenderViewEntity() == CameraEntity.this) {
                //#if MC>=10904
                //$$ event.setRoll(roll);
                //#else
                //$$ event.roll = roll;
                //#endif
        //$$     }
        //$$ }
        //#endif
        //#endif

        private boolean heldItemTooltipsWasTrue;

        //#if FABRIC>=1
        // FIXME fabric
        //#else
        //$$ @SubscribeEvent
        //$$ public void preRenderGameOverlay(RenderGameOverlayEvent.Pre event) {
        //$$     switch (event.getType()) {
        //$$         case ALL:
        //$$             heldItemTooltipsWasTrue = mc.gameSettings.heldItemTooltips;
        //$$             mc.gameSettings.heldItemTooltips = false;
        //$$             break;
        //$$         case ARMOR:
        //$$         case HEALTH:
        //$$         case FOOD:
        //$$         case AIR:
        //$$         case HOTBAR:
        //$$         case EXPERIENCE:
        //$$         case HEALTHMOUNT:
        //$$         case JUMPBAR:
                //#if MC>=10904
                //$$ case POTION_ICONS:
                //#endif
        //$$             event.setCanceled(true);
        //$$             break;
        //$$         case HELMET:
        //$$         case PORTAL:
        //$$         case CROSSHAIRS:
        //$$         case BOSSHEALTH:
                //#if MC>=10904
                //$$ case BOSSINFO:
                //$$ case SUBTITLES:
                //#endif
        //$$         case TEXT:
        //$$         case CHAT:
        //$$         case PLAYER_LIST:
        //$$         case DEBUG:
        //$$             break;
        //$$     }
        //$$ }
        //$$
        //$$ @SubscribeEvent
        //$$ public void postRenderGameOverlay(RenderGameOverlayEvent.Post event) {
        //$$     if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) return;
        //$$     mc.gameSettings.heldItemTooltips = heldItemTooltipsWasTrue;
        //$$ }
        //#endif
    }
}
