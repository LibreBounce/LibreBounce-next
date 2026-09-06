/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.librebounce.mixins.events;

import net.librebounce.event.EventManager;
import net.librebounce.event.Render2DEvent;
/*import net.librebounce.features.module.impl.render.AntiBlind;
import net.librebounce.features.module.impl.render.HUD;
import net.librebounce.features.module.impl.render.SilentHotbarModule;
import net.librebounce.ui.font.AWTFontRenderer;
import net.librebounce.utils.client.ClassUtils;
import net.librebounce.utils.inventory.SilentHotbar;
import net.librebounce.utils.inventory.InventoryUtils;
import net.librebounce.utils.render.ColorSettingsKt;
import net.librebounce.utils.render.RenderUtils;
import net.librebounce.utils.render.shader.shaders.GradientShader;
import net.librebounce.utils.render.shader.shaders.RainbowShader;*/
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiElement;
import net.minecraft.client.gui.GameGui;
import net.minecraft.client.render.Window;
import net.minecraft.client.render.platform.Lighting;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.entity.living.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static net.minecraft.client.render.platform.GlStateManager.*;
import static org.lwjgl.opengl.GL11.*;

@Mixin(GameGui.class)
public abstract class GameGuiMixin extends GuiElement {
	@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	private void libreBounce$injectRender2DEvent(float delta, CallbackInfo ci) {
		EventManager.INSTANCE.call(new Render2DEvent(delta));
	}
}
