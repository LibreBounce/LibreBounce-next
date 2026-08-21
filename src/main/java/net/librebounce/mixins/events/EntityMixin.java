package net.librebounce.mixins.events;

import net.librebounce.event.EventManager;
import net.librebounce.event.StrafeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin {
	@Shadow
	public double x;
	@Shadow
	public double y;
	@Shadow
	public double z;
	@Unique
	public double realX;
	@Unique
	public double realY;
	@Unique
	public double realZ;

	@Inject(method = "updateVelocity", at = @At("HEAD"), cancellable = true)
	private void libreBounce$strafeEvent(float strafe, float forward, float friction, final CallbackInfo callbackInfo) {
		//noinspection ConstantConditions
		if ((Object) this != Minecraft.getInstance().player) return;

		final StrafeEvent strafeEvent = new StrafeEvent(strafe, forward, friction);
		EventManager.INSTANCE.call(strafeEvent);

		if (strafeEvent.isCancelled()) callbackInfo.cancel();
	}
}
