package net.librebounce.mixins.events;

import net.librebounce.event.*;
import net.librebounce.utils.rotation.Rotation;
import net.librebounce.utils.rotation.RotationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.living.player.Input;
import net.minecraft.client.entity.living.player.LocalClientPlayerEntity;
import net.minecraft.client.network.handler.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalClientPlayerEntity.class)
public abstract class LocalClientPlayerEntityMixin extends Entity {
	@Shadow
	public Input input;

	@Shadow
	protected abstract boolean isCamera();

	@Shadow
	public boolean sentSprinting;
	/*@Shadow
	public int sprintingTicksLeft;
	@Shadow
	public float timeInPortal;
	@Shadow
	public float prevTimeInPortal;
	@Shadow
	public float horseJumpPower;
	@Shadow
	public int horseJumpPowerCounter;*/
	@Shadow
	@Final
	public ClientPlayNetworkHandler networkHandler;
	/*@Shadow
	protected int sprintToggleTimer;
	@Shadow
	protected Minecraft mc;
	@Shadow
	private boolean serverSneakState;
	@Shadow
	private double lastReportedPosX;
	@Shadow
	private int positionUpdateTicks;
	@Shadow
	private double lastReportedPosY;
	@Shadow
	private double lastReportedPosZ;*/

	@Shadow
	public double x;
	@Shadow
	public double y;
	@Shadow
	public double z;
	@Shadow
	public float yaw;
	@Shadow
	public float pitch;
	@Shadow
	public boolean onGround;

	@Shadow
	private float sentYaw;
	@Shadow
	private float sentPitch;

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
	@Inject(method = "sendMovementToServer", at = @At("HEAD"), cancellable = true)
	private void libreBounce$events(CallbackInfo ci) {
		/*MotionEvent motionEvent = new MotionEvent(
			x,
			getShape().minY,
			z,
			onGround,
			EventState.PRE
		);

		EventManager.INSTANCE.call(motionEvent);*/

		/*final InventoryMove inventoryMove = InventoryMove.INSTANCE;
		final Sneak sneak = Sneak.INSTANCE;
		final Derp derp = Derp.INSTANCE;

		final boolean fakeSprint = inventoryMove.handleEvents() && inventoryMove.getAacAdditionPro()
			|| AntiHunger.INSTANCE.handleEvents()
			|| sneak.handleEvents() && (!PlayerExtensionKt.isMoving(mc.player) || !sneak.getStopMove()) && sneak.getMode().equals("MineSecure")
			|| Disabler.INSTANCE.handleEvents() && Disabler.INSTANCE.getStartSprint();

		boolean sprinting = isSprinting() && !fakeSprint;

		if (sprinting != sentSprinting) {
			if (sprinting)
				sendQueue.addToSendQueue(new PlayerMovementActionC2SPacket((LocalClientPlayerEntity) (Object) this, START_SPRINTING));
			else sendQueue.addToSendQueue(new PlayerMovementActionC2SPacket((LocalClientPlayerEntity) (Object) this, STOP_SPRINTING));

			sentSprinting = sprinting;
		}

		boolean sneaking = isSneaking();

		if (sneaking != serverSneakState && (!sneak.handleEvents() || sneak.getMode().equals("Legit"))) {
			if (sneaking)
				sendQueue.addToSendQueue(new PlayerMovementActionC2SPacket((LocalClientPlayerEntity) (Object) this, START_SNEAKING));
			else sendQueue.addToSendQueue(new PlayerMovementActionC2SPacket((LocalClientPlayerEntity) (Object) this, STOP_SNEAKING));

			serverSneakState = sneaking;
		}*/

		/*final MovementUtils movementUtils = MovementUtils.INSTANCE;

		if (motionEvent.getOnGround()) {
			movementUtils.setGroundTicks(movementUtils.getGroundTicks() + 1);
			movementUtils.setAirTicks(0);
		} else {
			movementUtils.setGroundTicks(0);
			movementUtils.setAirTicks(movementUtils.getAirTicks() + 1);
		}*/

		if (isCamera()) {
			float yaw1 = yaw;
			float pitch1 = pitch;

			final Rotation currentRotation = RotationUtils.INSTANCE.getCurrentRotation();

			if (currentRotation != null) {
				yaw1 = currentRotation.getYaw();
				pitch1 = currentRotation.getPitch();
			}

			/*double xDiff = motionEvent.getX() - lastReportedPosX;
			double yDiff = motionEvent.getY() - lastReportedPosY;
			double zDiff = motionEvent.getZ() - lastReportedPosZ;*/
			double yawDiff = yaw - this.sentYaw;
			double pitchDiff = pitch - this.sentPitch;
			//boolean moved = xDiff * xDiff + yDiff * yDiff + zDiff * zDiff > 9.0E-4 || positionUpdateTicks >= 20;
			boolean rotated = /*!FreeCam.INSTANCE.shouldDisableRotations() && */(yawDiff != 0 || pitchDiff != 0);

			/*if (vehicle == null) {
				if (moved && rotated) {
					sendQueue.addToSendQueue(new PlayerMoveC2SPacket.PositionAndAngles(motionEvent.getX(), motionEvent.getY(), motionEvent.getZ(), yaw, pitch, motionEvent.getOnGround()));
				} else if (moved) {
					sendQueue.addToSendQueue(new PlayerMoveC2SPacket.Position(motionEvent.getX(), motionEvent.getY(), motionEvent.getZ(), motionEvent.getOnGround()));
				} else if (rotated) {*/
					networkHandler.sendPacket(new PlayerMoveC2SPacket.Angles(yaw, pitch, onGround));
				/*} else {
					sendQueue.addToSendQueue(new PlayerMoveC2SPacket(motionEvent.getOnGround()));
				}
			} else {
				sendQueue.addToSendQueue(new PlayerMoveC2SPacket.PositionAndAngles(velocityX, -999, velocityZ, yaw, pitch, motionEvent.getOnGround()));
				moved = false;
			}

			++positionUpdateTicks;

			if (moved) {
				lastReportedPosX = motionEvent.getX();
				lastReportedPosY = motionEvent.getY();
				lastReportedPosZ = motionEvent.getZ();
				positionUpdateTicks = 0;
			}*/

			//if (!FreeCam.INSTANCE.shouldDisableRotations()) {
				RotationUtils.INSTANCE.setServerRotation(new Rotation(yaw1, pitch1));
			//}

			/*if (rotated) {
				this.lastReportedYaw = yaw;
				this.lastReportedPitch = pitch;
			}*/
		}

		//EventManager.INSTANCE.call(new MotionEvent(x, getShape().minY, z, onGround, EventState.POST));

		EventManager.INSTANCE.call(RotationUpdateEvent.INSTANCE);

		ci.cancel();
	}

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

	@Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/living/player/ClientPlayerEntity;tick()V", shift = At.Shift.BEFORE, ordinal = 0), cancellable = true)
	private void preTickEvent(CallbackInfo ci) {
		final PlayerTickEvent tickEvent = new PlayerTickEvent(EventState.PRE);
		EventManager.INSTANCE.call(tickEvent);

		if (tickEvent.isCancelled()) {
			EventManager.INSTANCE.call(RotationUpdateEvent.INSTANCE);
			ci.cancel();
		}
	}

	@Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/living/player/ClientPlayerEntity;tick()V", shift = At.Shift.AFTER, ordinal = 0))
	private void postTickEvent(CallbackInfo ci) {
		final PlayerTickEvent tickEvent = new PlayerTickEvent(EventState.POST);
		EventManager.INSTANCE.call(tickEvent);
	}
}
