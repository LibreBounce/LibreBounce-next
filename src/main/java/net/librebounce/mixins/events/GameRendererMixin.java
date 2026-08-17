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
}
