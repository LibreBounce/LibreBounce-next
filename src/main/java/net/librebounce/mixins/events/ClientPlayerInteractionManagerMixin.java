package net.librebounce.mixins.events;

import net.librebounce.event.AttackEvent;
import net.librebounce.event.EventManager;
import net.minecraft.client.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.living.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {
	@Inject(method = "attackEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/ClientPlayerInteractionManager;updateSelectedHotbarSlot()V"))
	private void attackEntity(PlayerEntity entityPlayer, Entity targetEntity, CallbackInfo callbackInfo) {
		EventManager.INSTANCE.call(new AttackEvent(targetEntity));
	}
}
