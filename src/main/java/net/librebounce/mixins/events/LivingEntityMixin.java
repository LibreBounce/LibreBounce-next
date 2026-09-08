package net.librebounce.injection.forge.mixins.entity;

import net.librebounce.event.EventManager;
import net.librebounce.event.EventState;
import net.librebounce.event.JumpEvent;
//import net.librebounce.features.module.modules.movement.liquidwalk.LiquidWalk;
import net.librebounce.features.module.modules.movement.NoJumpDelay;
//import net.librebounce.features.module.modules.movement.Sprint;
//import net.librebounce.features.module.modules.render.Animations;
import net.librebounce.features.module.modules.render.Rotations;
//import net.librebounce.features.module.modules.world.scaffolds.Scaffold;
//import net.librebounce.features.module.modules.world.scaffolds.Tower;
import net.librebounce.utils.movement.MovementUtils;
import net.librebounce.utils.rotation.Rotation;
import net.librebounce.features.module.base.settings.RotationSettings;
import net.librebounce.utils.rotation.RotationUtils;
import net.librebounce.utils.extensions.MathExtensionsKt;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.living.player.LocalClientPlayerEntity;
import net.minecraft.entity.living.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.StatusEffect;
import net.minecraft.entity.living.effect.StatusEffectInstance;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntity {

    @Shadow
    public float headYaw;
    @Shadow
    public boolean jumping;
    @Shadow
    public int jumpingCooldown;

    @Shadow
    protected abstract float getJumpUpwardsMotion();

    @Shadow
    public abstract StatusEffectInstance getEffectInstance(StatusEffect potionIn);

    @Shadow
    public abstract boolean hasStatusEffect(StatusEffect potionIn);

    @Shadow
    public void onLivingUpdate() {
    }

    @Shadow
    protected abstract void checkFallDamage(double y, boolean onGroundIn, Block blockIn, BlockPos pos);

    @Shadow
    public abstract float getHealth();

    @Shadow
    public abstract ItemStack getDisplayItemInHand();

    @Shadow
    protected abstract void jumpInWater();

    /**
     * @author CCBlueX
     */
    @Overwrite
    protected void jump() {
        final JumpEvent prejumpEvent = new JumpEvent(getJumpUpwardsMotion(), EventState.PRE);
        if ((Object) this == Minecraft.getInstance().player) {
            EventManager.INSTANCE.call(prejumpEvent);
            if (prejumpEvent.isCancelled()) return;
        }

        velocityY = prejumpEvent.getMotion();

        if (hasStatusEffect(StatusEffect.jump))
            velocityY += (float) (getEffectInstance(StatusEffect.jump).getAmplifier() + 1) * 0.1F;

        if (isSprinting()) {
            float fixedYaw = this.yaw;

            final RotationUtils rotationUtils = RotationUtils.INSTANCE;
            final Rotation currentRotation = rotationUtils.getCurrentRotation();
            final RotationSettings rotationData = rotationUtils.getActiveSettings();
            if (currentRotation != null && rotationData != null && rotationData.getStrafe()) {
                fixedYaw = currentRotation.getYaw();
            }

            /*final Sprint sprint = Sprint.INSTANCE;
            if (sprint.handleEvents() && sprint.getMode().equals("Vanilla") && sprint.getAllDirections() && sprint.getJumpDirections()) {
                fixedYaw += MathExtensionsKt.toDegreesF(MovementUtils.INSTANCE.getDirection()) - this.yaw;
            }*/

            final float f = fixedYaw * 0.017453292F;
            velocityX -= MathHelper.sin(f) * 0.2F;
            velocityZ += MathHelper.cos(f) * 0.2F;
        }

        velocityDirty = true;

        if ((Object) this == Minecraft.getInstance().player) {
            final JumpEvent postjumpEvent = new JumpEvent((float) velocityY, EventState.POST);
            EventManager.INSTANCE.call(postjumpEvent);
        }
    }

    @Inject(method = "getRotationVec", at = @At("HEAD"), cancellable = true)
    private void getRotationVec(CallbackInfoReturnable<Vec3d> callbackInfoReturnable) {
        //noinspection ConstantConditions
        if (((LivingEntity) (Object) this) instanceof LocalClientPlayerEntity)
            callbackInfoReturnable.setReturnValue(getRotationVector(pitch, yaw));
    }

    /**
     * Inject head yaw rotation modification
     */
    @Inject(method = "mobTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;serverTickAi()V", shift = At.Shift.AFTER))
    private void hookHeadRotations(CallbackInfo ci) {
        Rotation rotation = Rotations.INSTANCE.getRotation();

        //noinspection ConstantValue
        this.headYaw = ((LivingEntity) (Object) this) instanceof LocalClientPlayerEntity && Rotations.INSTANCE.shouldUseRealisticMode() && rotation != null ? rotation.getYaw() : this.headYaw;
    }

    /**
     * Inject body rotation modification
     */
    @Redirect(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/LivingEntity;yaw:F", ordinal = 0))
    private float hookBodyRotationsA(LivingEntity instance) {
        Rotation rotation = Rotations.INSTANCE.getRotation();

        return instance instanceof LocalClientPlayerEntity && Rotations.INSTANCE.shouldUseRealisticMode() && rotation != null ? rotation.getYaw() : instance.yaw;
    }

    /**
     * Inject body rotation modification
     */
    @Redirect(method = "bodyMovement", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/LivingEntity;yaw:F"))
    private float hookBodyRotationsB(LivingEntity instance) {
        Rotation rotation = Rotations.INSTANCE.getRotation();

        return instance instanceof LocalClientPlayerEntity && Rotations.INSTANCE.shouldUseRealisticMode() && rotation != null ? rotation.getYaw() : instance.yaw;
    }
}