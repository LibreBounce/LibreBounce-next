package net.librebounce.mixins.events;

import io.netty.channel.ChannelHandlerContext;
import net.librebounce.event.EventManager;
import net.librebounce.event.EventState;
import net.librebounce.event.PacketEvent;
import net.minecraft.network.Connection;
import net.minecraft.network.packet.Packet;
import net.librebounce.utils.client.PPSCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public class ConnectionMixin {

	@Inject(method = "channelRead0", at = @At("HEAD"), cancellable = true)
	private void read(ChannelHandlerContext context, Packet<?> packet, CallbackInfo callback) {
		final PacketEvent event = new PacketEvent(packet, EventState.RECEIVE);
		EventManager.INSTANCE.call(event);

		if (event.isCancelled()) {
			callback.cancel();
			return;
		}

		PPSCounter.INSTANCE.registerType(PPSCounter.PacketType.RECEIVED);
	}

	@Inject(method = "send(Lnet/minecraft/network/packet/Packet;)V", at = @At("HEAD"), cancellable = true)
	private void send(Packet<?> packet, CallbackInfo callback) {
		final PacketEvent event = new PacketEvent(packet, EventState.SEND);
		EventManager.INSTANCE.call(event);

		if (event.isCancelled()) {
			callback.cancel();
			return;
		}

		PPSCounter.INSTANCE.registerType(PPSCounter.PacketType.SEND);
	}
}
