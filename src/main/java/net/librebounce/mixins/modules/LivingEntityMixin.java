package net.librebounce.mixins.modules;

import net.librebounce.features.module.impl.render.Rotations;
import net.librebounce.utils.rotation.Rotation;
import net.minecraft.client.entity.living.player.LocalClientPlayerEntity;
import net.minecraft.entity.living.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
	@Shadow
	public float headYaw;

	@Inject(method = "mobTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/living/LivingEntity;serverTickAi()V", shift = At.Shift.AFTER))
	private void hookHeadRotations(CallbackInfo ci) {
		Rotation rotation = Rotations.INSTANCE.getRotation();

		//noinspection ConstantValue
		this.headYaw = ((LivingEntity) (Object) this) instanceof LocalClientPlayerEntity && Rotations.INSTANCE.shouldUseRealisticMode() && rotation != null ? rotation.getYaw() : this.headYaw;
	}
}
