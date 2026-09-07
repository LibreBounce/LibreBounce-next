package net.librebounce.features.module.impl.combat

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import net.librebounce.features.module.base.Category
import net.librebounce.features.module.base.Module
import net.librebounce.features.module.impl.player.InventoryCleaner.canBeRepairedWithOther
import net.librebounce.utils.client.PacketUtils.sendPacket
import net.librebounce.utils.inventory.ArmorComparator.getBestArmorSet
import net.librebounce.utils.inventory.InventoryManager
import net.librebounce.utils.inventory.InventoryManager.autoArmorCurrentSlot
import net.librebounce.utils.inventory.InventoryManager.autoArmorLastSlot
import net.librebounce.utils.inventory.InventoryManager.canClickInventory
import net.librebounce.utils.inventory.InventoryManager.hasScheduledInLastLoop
import net.librebounce.utils.inventory.InventoryManager.passedPostInventoryCloseDelay
import net.librebounce.utils.inventory.InventoryUtils.isFirstInventoryClick
import net.librebounce.utils.inventory.InventoryUtils.serverOpenInventory
import net.librebounce.utils.inventory.InventoryUtils.toHotbarIndex
import net.librebounce.utils.inventory.SilentHotbar
import net.librebounce.utils.inventory.hasItemAgePassed
import net.librebounce.utils.timing.TickedActions.awaitTicked
import net.librebounce.utils.timing.TickedActions.clickNextTick
import net.librebounce.utils.timing.TickedActions.isTicked
import net.librebounce.utils.timing.TickedActions.nextTick
import net.minecraft.client.gui.screen.inventory.menu.SurvivalInventoryScreen
import net.minecraft.entity.living.LivingEntity.getEquipmentSlot
import net.minecraft.item.ItemStack
import net.minecraft.network.packet.c2s.play.PlayerUseC2SPacket

object AutoArmor : Module("AutoArmor", Category.COMBAT) {
    private val delay by intRange("Delay", 50..50, 0..1000, suffix = "ms")
    private val minItemAge by int("MinItemAge", 0, 0..2000, suffix = "ms")

    private val invOpen by +InventoryManager.invOpenValue
    private val simulateInventory by +InventoryManager.simulateInventoryValue

    private val postInventoryCloseDelay by +InventoryManager.postInventoryCloseDelayValue
    private val autoClose by +InventoryManager.autoCloseValue
    private val startDelay by +InventoryManager.startDelayValue
    private val closeDelay by +InventoryManager.closeDelayValue

    // When swapping armor pieces, it grabs the better one, drags and swaps it with equipped one and drops the equipped one (no time of having no armor piece equipped)
    // Has to make more clicks, works slower
    val smartSwap by boolean("SmartSwap", true)

    private val noMove by +InventoryManager.noMoveValue
    private val noMoveAir by +InventoryManager.noMoveAirValue
    private val noMoveGround by +InventoryManager.noMoveGroundValue

    private val hotbar by boolean("Hotbar", true)

    private val hotbarSlotSwitchDelay by intRange("HotbarSlotSwitchDelay", 50..50, 0..1000, suffix = "ms") { hotbar }

    // Prevents AutoArmor from hotbar equipping while any screen is open
    private val notInContainers by boolean("NotInContainers", false) { hotbar }

    val highlightSlot by +InventoryManager.highlightSlotValue
    val backgroundColor by +InventoryManager.borderColor

    val borderStrength by +InventoryManager.borderStrength
    val borderColor by +InventoryManager.borderColor

    suspend fun equipFromHotbar() {
        if (!shouldOperate(onlyHotbar = true)) {
            autoArmorCurrentSlot = -1
            autoArmorLastSlot = -1
            return
        }

        val player = mc.player ?: return

        var hasClickedHotbar = false

        val items = withContext(Dispatchers.Main) {
            player.menu.slots.map { it.item }
        }

        val bestArmorSet = getBestArmorSet(items) ?: return

        for (slot in 0..3) {
            val (index, item) = bestArmorSet[slot] ?: continue

            // Check if the armor piece is in the hotbar
            val hotbarIndex = index?.toHotbarIndex(items.size) ?: continue

            if (isTicked(index) || isTicked(slot + 5))
                continue

            if (!item.hasItemAgePassed(minItemAge))
                continue

            val armorPos = getEquipmentSlot(item) - 1

            // Check if target armor slot isn't occupied
            if (player.inventory.armor[armorPos] != null)
                continue

            hasClickedHotbar = true

            val equippingAction = {
                // Set current slot being stolen for highlighting
                autoArmorCurrentSlot = hotbarIndex

                SilentHotbar.selectSlotSilently(
                    this,
                    hotbarIndex,
                    immediate = true,
                    render = false,
                    resetManually = true
                )

                // Switch selected hotbar slot, right click to equip
                sendPacket(PlayerUseC2SPacket(item))

                // Instantly update inventory on client-side to prevent repetitive clicking because of ping
                player.inventory.armor[armorPos] = item
                player.inventory.items[hotbarIndex] = null
            }

            // Schedule hotbar click
            nextTick(action = equippingAction)

            delay(hotbarSlotSwitchDelay.random().toLong())
        }

        delay(delay.random().toLong())

        awaitTicked()

        // Sync selected slot next tick
        if (hasClickedHotbar)
            nextTick { SilentHotbar.resetSlot(this) }
    }

    suspend fun equipFromInventory() {
        if (!shouldOperate()) {
            autoArmorCurrentSlot = -1
            autoArmorLastSlot = -1
            return
        }

        val player = mc.player ?: return

        for (slot in 0..3) {
            if (!shouldOperate()) {
                autoArmorCurrentSlot = -1
                autoArmorLastSlot = -1
                return
            }

            val items = withContext(Dispatchers.Main) {
                player.menu.slots.map { it.item }
            }

            val armorSet = getBestArmorSet(items) ?: continue

            // Shouldn't iterate over armor set because after waiting for nomove and invopen it could be outdated
            val (index, item) = armorSet[slot] ?: continue

            // Index is null when searching in chests for already equipped armor to prevent any accidental impossible interactions
            index ?: continue

            // Check if best item is already scheduled to be equipped next tick
            if (isTicked(index) || isTicked(slot + 5))
                continue

            if (!item.hasItemAgePassed(minItemAge))
                continue

            // Don't equip if it can be repaired with other armor piece, wait for the repair to happen first
            // Armor piece will then get equipped right after the repair
            if (canBeRepairedWithOther(item, items))
                continue

            // Set current slot being stolen for highlighting
            autoArmorCurrentSlot = index

            when (items[slot + 5]) {
                // Best armor is already equipped
                item -> {
                    autoArmorCurrentSlot = -1
                    autoArmorLastSlot = -1
                    continue
                }

                // No item is equipped in armor slot
                null ->
                    // Equip by shift-clicking
                    click(index, 0, 1)

                else -> {
                    if (smartSwap) {
                        // Player has worse armor equipped, drag the best armor, swap it with currently equipped armor and drop the bad armor
                        // This way there is no time of having no armor (but more clicks)

                        // Grab better armor
                        click(index, 0, 0)

                        // Swap it with currently equipped armor
                        click(slot + 5, 0, 0)

                        // Drop worse item by dragging and dropping it
                        click(-999, 0, 0)
                    } else {
                        // Normal version

                        // Drop worse armor
                        click(slot + 5, 0, 4)

                        // Equip better armor
                        click(index, 0, 1)
                    }
                }
            }
        }

        // Wait till all scheduled clicks were sent
        awaitTicked()
    }

    fun equipFromHotbarInChest(hotbarIndex: Int?, item: ItemStack) {
        // AutoArmor is disabled or prohibited from equipping while in containers
        if (hotbarIndex == null || !canEquipFromChest()) {
            autoArmorCurrentSlot = -1
            autoArmorLastSlot = -1
            return
        }

        // Set current slot being stolen for highlighting
        autoArmorCurrentSlot = hotbarIndex

        SilentHotbar.selectSlotSilently(this, hotbarIndex, immediate = true, render = false, resetManually = true)

        sendPacket(PlayerUseC2SPacket(item))
    }

    fun canEquipFromChest() = handleEvents() && hotbar && !notInContainers

    private suspend fun shouldOperate(onlyHotbar: Boolean = false): Boolean {
        while (true) {
            if (!handleEvents())
                return false

            if (!passedPostInventoryCloseDelay)
                return false

            if (mc.interactionManager?.gameMode?.isSurvivalOrAdventure != true)
                return false

            // It is impossible to equip armor when a container is open; only try to equip by right-clicking from hotbar (if NotInContainers is disabled)
            if (mc.player?.menu?.networkId != 0 && (!onlyHotbar || notInContainers))
                return false

            // Player doesn't need to have inventory open or not to move, when equipping from hotbar
            if (onlyHotbar)
                return hotbar

            if (invOpen && mc.screen !is SurvivalInventoryScreen)
                return false

            // Wait till NoMove check isn't violated
            if (canClickInventory(closeWhenViolating = true))
                return true

            // If NoMove is violated, wait a tick and check again
            // If there is no delay, very weird things happen: https://www.guilded.gg/CCBlueX/groups/1dgpg8Jz/channels/034be45e-1b72-4d5a-bee7-d6ba52ba1657/chat?messageId=94d314cd-6dc4-41c7-84a7-212c8ea1cc2a
            delay(50)
        }
    }

    private suspend fun click(slot: Int, button: Int, mode: Int, allowDuplicates: Boolean = false) {
        // Wait for NoMove or cancel click
        if (!shouldOperate())
            return

        if (simulateInventory || invOpen)
            serverOpenInventory = true

        if (isFirstInventoryClick) {
            // Have to set this manually, because it would delay all clicks until a first scheduled click was sent
            isFirstInventoryClick = false

            delay(startDelay.random().toLong())
        }

        clickNextTick(slot, button, mode, allowDuplicates)

        hasScheduledInLastLoop = true

        delay(delay.random().toLong())
    }
}