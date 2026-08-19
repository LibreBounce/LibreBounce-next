package net.librebounce.utils.inventory

import net.librebounce.event.*
import net.librebounce.features.module.impl.misc.NoSlotSet
//import net.librebounce.features.module.impl.render.SilentHotbarModule
//import net.librebounce.features.module.impl.world.ChestAura
import net.librebounce.utils.client.MinecraftInstance
//import net.librebounce.utils.client.PacketUtils.sendPacket
import net.librebounce.utils.extensions.lerpWith
import net.librebounce.utils.render.RenderUtils
import net.librebounce.utils.timing.MSTimer
import net.librebounce.utils.timing.WaitTickUtils
import net.minecraft.block.PlantBlock
import net.minecraft.block.Blocks.*
import net.minecraft.client.Minecraft
import net.minecraft.item.Item
import net.minecraft.item.BlockItem
import net.minecraft.network.packet.c2s.play.PlayerUseC2SPacket
import net.minecraft.network.packet.c2s.play.CloseInventoryMenuC2SPacket
import net.minecraft.network.packet.c2s.play.InventoryMenuClickSlotC2SPacket
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket.Status.OPEN_INVENTORY_ACHIEVEMENT
import net.minecraft.network.packet.s2c.play.SelectSlotS2CPacket
import net.minecraft.network.packet.s2c.play.OpenInventoryMenuS2CPacket
import net.minecraft.network.packet.s2c.play.CloseInventoryMenuS2CPacket

object InventoryUtils : Listenable {
    val mc = Minecraft.getInstance()

    // Is inventory open on server-side?
    var serverOpenInventory
        get() = _serverOpenInventory
        set(value) {
            if (value != _serverOpenInventory) {
                /*sendPacket(
                    if (value) ClientStatusC2SPacket(OPEN_INVENTORY_ACHIEVEMENT)
                    else CloseInventoryMenuC2SPacket(mc.player?.menu?.networkId ?: 0)
                )*/

                _serverOpenInventory = value
            }
        }

    var serverOpenContainer = false
        private set

    // Backing fields
    private var _serverOpenInventory = false

    var lerpedSlot = 0f

    var isFirstInventoryClick = true

    var timeSinceClosedInventory = 0L

    val CLICK_TIMER = MSTimer()

    val BLOCK_BLACKLIST = setOf(
        CHEST,
        ENDER_CHEST,
        TRAPPED_CHEST,
        ANVIL,
        SAND,
        WEB,
        TORCH,
        CRAFTING_TABLE,
        FURNACE,
        LILY_PAD,
        DISPENSER,
        STONE_PRESSURE_PLATE,
        WOODEN_PRESSURE_PLATE,
        NOTEBLOCK,
        DROPPER,
        TNT,
        STANDING_BANNER,
        WALL_BANNER,
        REDSTONE_TORCH,
        LADDER
    )

    fun findItemArray(startInclusive: Int, endInclusive: Int, items: Array<Item>): Int? {
        for (i in startInclusive..endInclusive)
            if (mc.player.menu.getSlot(i).item?.item in items)
                return i - 36

        return null
    }

    fun findItem(start: Int, end: Int, item: Item): Int? {
        for (i in start..end)
            if (mc.player.menu.getSlot(i).item?.item == item)
                return i - if (start == 36 && end == 44) 36 else 0

        return null
    }

    fun hasSpaceInHotbar(): Boolean {
        for (i in 36..44)
            mc.player.menu.getSlot(i).item ?: return true

        return false
    }

    fun hasSpaceInInventory() = mc.player?.inventory?.emptySlot != -1

    fun countSpaceInInventory() = mc.player.inventory.items.count { it.isEmpty() }

    fun findBlockInHotbar(): Int? {
        val player = mc.player ?: return null
        val inventory = player.menu

        return (36..44).filter {
            val stack = inventory.getSlot(it).item ?: return@filter false
            val block = if (stack.item is BlockItem) (stack.item as BlockItem).block else return@filter false

            stack.item is BlockItem && stack.size > 0 && block !in BLOCK_BLACKLIST && block !is PlantBlock
        }.minByOrNull { (inventory.getSlot(it).item.item as BlockItem).block.isCube }?.minus(36)
    }

    fun findLargestBlockStackInHotbar(): Int? {
        val player = mc.player ?: return null
        val inventory = player.menu

        return (36..44).filter {
            val stack = inventory.getSlot(it).item ?: return@filter false
            val block = if (stack.item is BlockItem) (stack.item as BlockItem).block else return@filter false

            stack.item is BlockItem && stack.size > 0 && block.isCube && block !in BLOCK_BLACKLIST && block !is PlantBlock
        }.maxByOrNull { inventory.getSlot(it).item.size }?.minus(36)
    }

    fun findBlockStackInHotbarGreaterThan(amount: Int): Int? {
        val player = mc.player ?: return null
        val inventory = player.menu

        return (36..44).filter {
            val stack = inventory.getSlot(it).item ?: return@filter false
            val block = if (stack.item is BlockItem) (stack.item as BlockItem).block else return@filter false

            stack.item is BlockItem && stack.size > amount && block.isCube && block !in BLOCK_BLACKLIST && block !is PlantBlock
        }.minByOrNull { (inventory.getSlot(it).item.item as BlockItem).block.isCube }?.minus(36)
    }

    // Converts container slot to hotbar slot id, else returns null
    fun Int.toHotbarIndex(stacksSize: Int): Int? {
        val parsed = this - stacksSize + 9

        return if (parsed in 0..8) parsed else null
    }

    fun blocksAmount(): Int {
        val player = mc.player ?: return 0
        var amount = 0

        for (i in 36..44) {
            val stack = player.inventorySlot(i).item ?: continue
            val item = stack.item
            if (item is BlockItem) {
                val block = item.block
                val displayItemInHand = player.displayItemInHand
                if (displayItemInHand != null && displayItemInHand == stack || block !in BLOCK_BLACKLIST && block !is PlantBlock) {
                    amount += stack.size
                }
            }
        }

        return amount
    }

    val onPacket = handler<PacketEvent> { event ->
        if (event.isCancelled) return@handler

        when (val packet = event.packet) {
            is PlayerUseC2SPacket, is InventoryMenuClickSlotC2SPacket -> {
                CLICK_TIMER.reset()

                if (packet is InventoryMenuClickSlotC2SPacket)
                    isFirstInventoryClick = false
            }

            is ClientStatusC2SPacket ->
                if (packet.status == OPEN_INVENTORY_ACHIEVEMENT) {
                    if (_serverOpenInventory) event.cancelEvent()
                    else {
                        isFirstInventoryClick = true
                        _serverOpenInventory = true
                    }
                }

            is CloseInventoryMenuC2SPacket, is CloseInventoryMenuS2CPacket, is OpenInventoryMenuS2CPacket -> {
                isFirstInventoryClick = false
                _serverOpenInventory = false
                serverOpenContainer = false

                timeSinceClosedInventory = System.currentTimeMillis()

                if (packet is OpenInventoryMenuS2CPacket) {
                    if (packet.type == "minecraft:chest" || packet.type == "minecraft:container")
                        serverOpenContainer = true
                } //else
                    //ChestAura.tileTarget = null
            }

            is SelectSlotS2CPacket -> {
                if (SilentHotbar.currentSlot == packet.slot)
                    return@handler

                SilentHotbar.ignoreSlotChange = true

                val previousSlot = SilentHotbar.currentSlot

                if (NoSlotSet.handleEvents()) {
                    WaitTickUtils.conditionalSchedule {
                        if (SilentHotbar.currentSlot == packet.slot) {
                            mc.player?.inventory?.selectedSlot = previousSlot

                            return@conditionalSchedule true
                        }

                        false
                    }
                }
            }
        }
    }

    /*val onRender3D = handler<Render3DEvent> {
        val module = SilentHotbarModule

        val slotToUse = SilentHotbar.renderSlot(module.handleEvents() && module.keepHotbarSlot).toFloat()

        lerpedSlot = (lerpedSlot..slotToUse).lerpWith(RenderUtils.deltaTimeNormalized())
    }*/

    val onWorld = handler<WorldEvent> {
        SilentHotbar.resetSlot()

        _serverOpenInventory = false
        serverOpenContainer = false
    }


}
