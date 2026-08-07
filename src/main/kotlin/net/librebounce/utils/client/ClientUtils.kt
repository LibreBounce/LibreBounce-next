/*
 * LibreBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LibreBounce/
 */
package net.librebounce.utils.client

import net.librebounce.LibreBounce.CLIENT_NAME
import net.minecraft.client.options.GameOptions
import net.minecraft.network.Connection
import net.minecraft.network.packet.c2s.login.KeyC2SPacket
import net.minecraft.network.packet.s2c.login.HelloS2CPacket
import net.minecraft.text.LiteralText
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.lang.reflect.Field
import java.security.PublicKey
import javax.crypto.SecretKey

object ClientUtils : MinecraftInstance {
    var runTimeTicks = 0

    var profilerName = ""

    val LOGGER: Logger = LogManager.getLogger("LibreBounce")

    /*fun sendEncryption(
        networkManager: Connection,
        secretKey: SecretKey?,
        publicKey: PublicKey?,
        encryptionRequest: HelloS2CPacket
    ) {
        networkManager.sendPacket(KeyC2SPacket(secretKey, publicKey, encryptionRequest.verifyToken),
            { networkManager.enableEncryption(secretKey) }
        )
    }*/

    fun displayChatMessage(message: String) {
        mc.player?.sendMessage(LiteralText("§8[§9§l$CLIENT_NAME§8]§r $message"))
            ?: LOGGER.info("(MCChat) $message")
    }
}

fun chat(message: String) = ClientUtils.displayChatMessage(message)
