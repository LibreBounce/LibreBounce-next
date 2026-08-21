package net.librebounce.mixins.modules;

import net.librebounce.LibreBounce;
import net.librebounce.event.*;
import net.librebounce.features.module.impl.combat.AutoClicker;
import net.librebounce.utils.attack.CPSCounter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.main.RunArgs;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
	@Shadow
	private int attackCooldown;

	@Inject(method = "doAttack", at = @At("HEAD"))
	private void doAttack(CallbackInfo callbackInfo) {
		if (AutoClicker.INSTANCE.handleEvents()) {
			attackCooldown = 0;
		}

		if (attackCooldown <= 0) {
			CPSCounter.INSTANCE.registerClick(CPSCounter.MouseButton.LEFT);
		}
	}
}
