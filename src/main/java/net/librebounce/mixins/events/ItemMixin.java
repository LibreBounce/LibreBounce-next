package net.librebounce.mixins.events;

import net.librebounce.utils.rotation.Rotation;
import net.librebounce.utils.rotation.RotationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Item.class)
public class ItemMixin {

    /**
     * Rotation modification injections. Replaces actual rotation with the current rotation to synchronize placements client-side.
     */
    // Originally getHitResultFromPlayer
    @Redirect(method = "getUseTarget", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/PlayerEntity;yaw:F"))
    private float libreBounce$hookCurrentRotationYaw(PlayerEntity instance) {
        Rotation rotation = RotationUtils.INSTANCE.getCurrentRotation();

        if (instance.getGameProfile() != Minecraft.getInstance().player.getGameProfile() || rotation == null) {
            return instance.yaw;
        }

        return rotation.getYaw();
    }

    @Redirect(method = "getUseTarget", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/PlayerEntity;pitch:F"))
    private float libreBounce$hookCurrentRotationPitch(PlayerEntity instance) {
        Rotation rotation = RotationUtils.INSTANCE.getCurrentRotation();

        if (instance.getGameProfile() != Minecraft.getInstance().player.getGameProfile() || rotation == null) {
            return instance.pitch;
        }

        return rotation.getPitch();
    }
}