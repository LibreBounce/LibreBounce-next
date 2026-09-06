package net.librebounce.mixins.events;

import net.librebounce.event.EventManager;
import net.librebounce.event.Render3DEvent;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Shadow
    private Entity targetEntity;

    @Shadow
    private Minecraft mc;

	@Inject(method = "render(IFJ)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", shift = At.Shift.AFTER))
	private void libreBounce$render3DEvent(int pass, float partialTicks, long finishTimeNano, CallbackInfo callbackInfo) {
        /*
          This is done so it supports Opti-Fine while also supporting any mod that cancels the ForgeHooksClient.renderFirstPersonHand event.
          For example, OrangeMarshall's 1.7 Animations mod.
         */
		/*if (ClientUtils.INSTANCE.getProfilerName().equals("hand")) {
			FreeLook.INSTANCE.runWithoutSavingRotations(() -> {
				FreeLook.INSTANCE.restoreOriginalRotation();*/
				EventManager.INSTANCE.call(new Render3DEvent(partialTicks));
				/*FreeLook.INSTANCE.useModifiedRotation();
				return null;
			});
		}*/
	}

	@Inject(method = "pick", at = @At("HEAD"), cancellable = true)
    private void libreBounce$rotationOverriding(float p_getMouseOver_1_, CallbackInfo ci) {
        Entity entity = mc.getRenderViewEntity();
        if (entity != null && mc.world != null) {
            mc.profiler.startSection("pick");
            mc.targetEntity = null;

            final Reach reach = Reach.INSTANCE;

            double d0 = reach.handleEvents() ? reach.getMaxRange() : mc.interactionManager.getBlockReachDistance();
            Vec3d vec3 = entity.getEyePosition(p_getMouseOver_1_);
            Rotation rotation = new Rotation(mc.player.yaw, mc.player.pitch);
            Vec3d vec31 = RotationUtils.INSTANCE.getRotationVector(RotationUtils.INSTANCE.getCurrentRotation() != null && OverrideRaycast.INSTANCE.shouldOverride() ? RotationUtils.INSTANCE.getCurrentRotation() : rotation);
            double p_rayTrace_1_ = (reach.handleEvents() ? reach.getBuildReach() : d0);
            Vec3d vec32 = vec3.addVector(vec31.x * p_rayTrace_1_, vec31.y * p_rayTrace_1_, vec31.z * p_rayTrace_1_);
            mc.crosshairTarget = entity.world.rayTrace(vec3, vec32, false, false, true);
            double d1 = d0;
            boolean flag = false;
            if (mc.interactionManager.extendedReach()) {
                // d0 = 6;
                d1 = 6;
            } else if (d0 > 3) {
                flag = true;
            }

            if (mc.crosshairTarget != null) {
                d1 = mc.crosshairTarget.facePos.distanceTo(vec3);
            }

            if (reach.handleEvents()) {
                double p_rayTrace_1_2 = reach.getBuildReach();
                Vec3d vec322 = vec3.addVector(vec31.x * p_rayTrace_1_2, vec31.y * p_rayTrace_1_2, vec31.z * p_rayTrace_1_2);
                final HitResult movingObjectPosition = entity.world.rayTrace(vec3, vec322, false, false, true);

                if (movingObjectPosition != null) d1 = movingObjectPosition.facePos.distanceTo(vec3);
            }

            targetEntity = null;
            Vec3d vec33 = null;
            List<Entity> list = mc.world.getEntities(Entity.class, Predicates.and(EntityFilter.NOT_SPECTATING, p_apply_1_ -> p_apply_1_ != null && p_apply_1_.canBeCollidedWith() && p_apply_1_ != entity));
            double d2 = d1;

            for (Entity entity1 : list) {
                float f1 = entity1.getCollisionBorderSize();

                final ArrayList<Box> boxes = new ArrayList<>();
                boxes.add(entity1.getShape().expand(f1, f1, f1));

                ForwardTrack.INSTANCE.includeEntityTruePos(entity1, () -> {
                    boxes.add(entity1.getShape().expand(f1, f1, f1));
                    return null;
                });

                for (final Box axisalignedbb : boxes) {
                    HitResult movingobjectposition = axisalignedbb.clip(vec3, vec32);
                    if (axisalignedbb.contains(vec3)) {
                        if (d2 >= 0) {
                            targetEntity = entity1;
                            vec33 = movingobjectposition == null ? vec3 : movingobjectposition.facePos;
                            d2 = 0;
                        }
                    } else if (movingobjectposition != null) {
                        double d3 = vec3.distanceTo(movingobjectposition.facePos);
                        if (d3 < d2 || d2 == 0) {
                            if (entity1 == entity.vehicle && !entity.canRiderInteract()) {
                                if (d2 == 0) {
                                    targetEntity = entity1;
                                    vec33 = movingobjectposition.facePos;
                                }
                            } else {
                                targetEntity = entity1;
                                vec33 = movingobjectposition.facePos;
                                d2 = d3;
                            }
                        }
                    }
                }
            }

            if (targetEntity != null && flag && vec3.distanceTo(vec33) > (reach.handleEvents() ? reach.getCombatReach() : 3)) {
                targetEntity = null;
                mc.crosshairTarget = new HitResult(HitResult.Type.MISS, Objects.requireNonNull(vec33), null, new BlockPos(vec33));
            }

            if (targetEntity != null && (d2 < d1 || mc.crosshairTarget == null)) {
                mc.crosshairTarget = new HitResult(targetEntity, vec33);
                if (targetEntity instanceof LivingEntity || targetEntity instanceof ItemFrameEntity) {
                    mc.targetEntity = targetEntity;
                }
            }

            mc.profiler.endSection();
        }

        ci.cancel();
    }

}
