package net.librebounce.mixins.init;

import net.librebounce.LibreBounce;
import net.minecraft.client.Minecraft;
import net.minecraft.client.main.RunArgs;
import org.lwjgl.opengl.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
	@Inject(method = "<init>", at = @At("TAIL"))
	private void libreBounce$onClientInit(RunArgs runArgs, CallbackInfo ci) {
		LibreBounce.INSTANCE.init();
	}

	@Inject(method = "initDisplay", at = @At("TAIL"))
	public void libreBounce$setWindowTitle(CallbackInfo ci) {
		Display.setTitle(LibreBounce.INSTANCE.getClientTitle());
	}

	@Inject(method = "shutdown", at = @At("HEAD"))
	private void shutdown(CallbackInfo callbackInfo) {
		LibreBounce.INSTANCE.exit();
	}
}
