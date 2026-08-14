/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.librebounce.mixins.events;

import net.librebounce.features.command.CommandManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class ScreenMixin {
	@Inject(method = "sendChatMessage(Ljava/lang/String;Z)V", at = @At("HEAD"), cancellable = true)
	private void libreBounce$commandManager(String msg, boolean addToChat, final CallbackInfo callbackInfo) {
		if (msg.startsWith(CommandManager.INSTANCE.getPrefix()) && addToChat) {
			Minecraft.getInstance().gui.getChat().addRecentMessage(msg);

			CommandManager.INSTANCE.executeCommands(msg);
			callbackInfo.cancel();
		}
	}
}
