package net.librebounce.mixins.init;

import net.librebounce.LibreBounce;
import net.librebounce.event.EventManager;
import net.librebounce.event.GameTickEvent;
import net.librebounce.event.KeyEvent;
import net.librebounce.event.TickEndEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.main.RunArgs;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
	@Shadow
	public Screen screen;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void libreBounce$onClientInit(RunArgs runArgs, CallbackInfo ci) {
		LibreBounce.INSTANCE.init();
	}

	@Inject(method = "initDisplay", at = @At("TAIL"))
	public void libreBounce$setWindowTitle(CallbackInfo ci) {
		Display.setTitle(LibreBounce.INSTANCE.getClientTitle());
	}

	@Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;handleGuiKeyBindings()V", shift = At.Shift.AFTER))
	private void onKey(CallbackInfo callbackInfo) {
		if (Keyboard.getEventKeyState() && screen == null)
			EventManager.INSTANCE.call(new KeyEvent(Keyboard.getEventKey() == 0 ? Keyboard.getEventCharacter() + 256 : Keyboard.getEventKey()));
	}

	@Inject(method = "tick", at = @At("TAIL"))
	private void injectEndTickEvent(CallbackInfo ci) {
		EventManager.INSTANCE.call(TickEndEvent.INSTANCE);
	}

	@Inject(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;joinPlayerCounter:I", ordinal = 0))
	private void onTick(final CallbackInfo callbackInfo) {
		EventManager.INSTANCE.call(GameTickEvent.INSTANCE);
	}

	@Inject(method = "shutdown", at = @At("HEAD"))
	private void shutdown(CallbackInfo callbackInfo) {
		LibreBounce.INSTANCE.exit();
	}
}
