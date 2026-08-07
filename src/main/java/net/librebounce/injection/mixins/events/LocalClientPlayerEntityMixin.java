package net.librebounce.injection.mixins.events;

import net.librebounce.event.*;
import net.minecraft.client.entity.living.player.Input;
import net.minecraft.client.entity.living.player.LocalClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalClientPlayerEntity.class)
public abstract class LocalClientPlayerEntityMixin {
	@Shadow
	public Input input;

	/**
	 * @author CCBlueX
	 */
	/*@Inject(method = "sendMovementToServer", at = @At("HEAD"), cancellable = true)
	private void sendMovementToServer(CallbackInfo ci) {
		MotionEvent motionEvent = new MotionEvent(
			x,
			getShape().minY,
			z,
			onGround,
			EventState.PRE
		);

		EventManager.INSTANCE.call(motionEvent);
	}*/

	@Inject(method = "mobTick()V", at = @At("HEAD"))
	private void libreBounce$updateEvents(CallbackInfo ci) {
		EventManager.INSTANCE.call(UpdateEvent.INSTANCE);

		Input modifiedInput = new Input();

		if (input.sneaking) {
			final SneakSlowDownEvent sneakSlowDownEvent = new SneakSlowDownEvent(input.movementSideways, input.movementForward);
			EventManager.INSTANCE.call(sneakSlowDownEvent);
			input.movementSideways = sneakSlowDownEvent.getStrafe();
			input.movementForward = sneakSlowDownEvent.getForward();
			// Add the sneak effect back
			modifiedInput.movementForward *= 0.3f;
			modifiedInput.movementSideways *= 0.3f;
			// Call again the event but this time have the modifiedInput
			final SneakSlowDownEvent secondSneakSlowDownEvent = new SneakSlowDownEvent(modifiedInput.movementSideways, modifiedInput.movementForward);
			EventManager.INSTANCE.call(secondSneakSlowDownEvent);
			modifiedInput.movementSideways = secondSneakSlowDownEvent.getStrafe();
			modifiedInput.movementForward = secondSneakSlowDownEvent.getForward();
		}
	}
}
