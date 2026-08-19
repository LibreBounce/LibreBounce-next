package net.librebounce.utils.inventory

import net.librebounce.utils.client.MinecraftInstance
import net.minecraft.client.Minecraft
import net.minecraft.enchantment.Enchantment
import net.minecraft.entity.living.player.PlayerEntity
import net.minecraft.inventory.menu.InventoryMenu
import net.minecraft.inventory.slot.InventorySlot
import net.minecraft.item.*
import net.minecraft.nbt.SnbtParser
import net.minecraft.resource.Identifier
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.math.roundToInt

object ItemUtils {
    val mc = Minecraft.getInstance()
    /**
     * Allows you to create an item using the item json
     *
     * @param itemArguments arguments of item
     * @return created item
     */
    /*fun createItem(itemArguments: String): ItemStack? {
        return try {
            val args = itemArguments.replace('&', '§').split(" ")

            val amount = args.getOrNull(1)?.toIntOrNull() ?: 1
            val meta = args.getOrNull(2)?.toIntOrNull() ?: 0

            val resourceLocation = Identifier(args[0])
            val item = Item.REGISTRY.get(resourceLocation) ?: return null

            val cursorItem = ItemStack(item, amount, meta)

            if (args.size >= 4) {
                val nbt = args.drop(3).joinToString(" ")

                cursorItem.metadata = SnbtParser.parseNbtEntry(nbt)
            }

            cursorItem
        } catch (exception: Exception) {
            exception.printStackTrace()
            null
        }
    }*/

    fun getItems(
        startInclusive: Int = 0, endInclusive: Int = 44, itemDelay: Int? = null,
        filter: ((ItemStack, Int) -> Boolean)? = null,
    ): Map<Int, ItemStack> {
        val items = mutableMapOf<Int, ItemStack>()

        for (i in startInclusive..endInclusive) {
            val cursorItem = mc.player.inventorySlot(i).item ?: continue

            if (cursorItem.isEmpty())
                continue

            if (itemDelay != null && !cursorItem.hasItemAgePassed(itemDelay))
                continue

            if (filter?.invoke(cursorItem, i) != false)
                items[i] = cursorItem
        }

        return items
    }


    /**
     * Allows you to check if player is consuming item
     */
    fun isConsumingItem(): Boolean {
        val usingItem = mc.player.itemInUse.item

        return mc.player.isUsingItem && (usingItem is FoodItem || usingItem is MilkBucketItem || usingItem is PotionItem)
    }
}

/**
 *
 * Item extensions
 *
 */

val ItemStack.durability
    get() = maxDamage - damage

// Calculates how much estimated durability does the item have thanks to its unbreaking level
/*val ItemStack.totalDurability: Int
    get() {
        // See https://minecraft.wiki/w/Unbreaking or https://minecraft.fandom.com/wiki/Unbreaking?oldid=2326887
        val multiplier =
            if (item is ArmorItem) 1 / (0.6 + (0.4 / (getEnchantmentLevel(Enchantment.UNBREAKING) + 1)))
            else getEnchantmentLevel(Enchantment.UNBREAKING) + 1.0

        return (multiplier * durability).roundToInt()
    }*/

/*val ItemStack.enchantments: Map<Enchantment, Int>
    get() {
        val enchantments = mutableMapOf<Enchantment, Int>()

        if (this.nbt == null || nbt.isEmpty)
            return enchantments

        repeat(nbt.elements.size) {
            val features = nbt.getCompound(it)
            if (features.contains("ench") || features.contains("id"))
                enchantments[Enchantment.byId(features.getInt("id"))] = features.getInt("lvl")
        }

        return enchantments
    }*/

val ItemStack.enchantmentCount
    get() = enchantments.size()

// Returns sum of levels of all enchantment levels
/*val ItemStack.enchantmentSum
    get() = enchantments.elements.sum()*/

//fun ItemStack.getEnchantmentLevel(enchantment: Enchantment) = enchantments.getOrDefault(enchantment, 0)

// Makes Kotlin smart-cast the stack to not null ItemStack
@OptIn(ExperimentalContracts::class)
fun ItemStack?.isEmpty(): Boolean {
    contract {
        returns(false) implies (this@isEmpty != null)
    }

    return this == null || item == null
}

@Suppress("KotlinConstantConditions")
fun ItemStack?.hasItemAgePassed(delay: Int) =
    this == null || System.currentTimeMillis() - popAnimationTime >= delay

/*val ItemStack.attackDamage
    get() = (attributeModifiers["generic.attackDamage"].firstOrNull()?.value ?: 1.0) +
            1.25 * getEnchantmentLevel(Enchantment.SHARPNESS)*/

fun ItemStack.isSplashPotion() = item is PotionItem && PotionItem.isSplashPotion(metadata)

operator fun InventoryMenu.get(range: IntRange): List<InventorySlot> = range.map(::getSlot)

fun PlayerEntity.inventorySlot(slot: Int) = menu.getSlot(slot)!!
fun PlayerEntity.hotBarSlot(slot: Int) = inventorySlot(slot + 36)
