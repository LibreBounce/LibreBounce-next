package net.librebounce.mixins.client;

import io.github.axolotlclient.rendering.DrawUtil;
import io.github.axolotlclient.rendering.font.DefaultFont;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.TextRenderer;
import net.minecraft.client.render.platform.GlStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import java.awt.Color;

@Mixin(ButtonWidget.class)
public class ButtonWidgetMixin {
	@Shadow
	public boolean active;
	@Shadow
	public boolean visible;
	@Shadow
	protected int height;
	@Shadow
	protected int width;
	@Shadow
	public int x;
	@Shadow
	public int y;
	@Shadow
	public String message;

	@Overwrite
	public void render(Minecraft minecraft, int mouseX, int mouseY) {
		if (!visible) return;

		boolean hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;

		DrawUtil.get().axolotlclient_rendering$roundedRect(
			x + width / 2,
			y,
			width / 2,
			y + height,
			hovered ? new Color(100, 100, 100, 150).getRGB() : new Color(255, 255, 255, 150).getRGB(),
			1f
		);

		Color textColor = new Color(255, 255, 255, 255);

		if (!active) {
			textColor = new Color(100, 100, 100, 255);
		} else if (hovered) {
			textColor = new Color(200, 255, 200, 255);
		}

		DrawUtil.get().axolotlclient_rendering$drawCenteredString(
			DefaultFont.inter(),
			message,
			x + width / 2,
			y + (float) (height - 8) / 2,
			textColor.getRGB()
		);
	}
}
