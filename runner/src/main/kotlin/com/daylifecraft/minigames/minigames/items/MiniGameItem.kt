package com.daylifecraft.minigames.minigames.items

import com.daylifecraft.common.text.PlayerText
import com.daylifecraft.common.util.extensions.miniMessage
import com.daylifecraft.minigames.minigames.items.MiniGameItem.PlayerItemInteraction
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import net.minestom.server.entity.Player
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.network.packet.server.play.SetCooldownPacket
import net.minestom.server.utils.time.Cooldown
import java.time.Duration
import java.util.UUID

class MiniGameItem(
  var material: Material,
  var amount: Int,
  private val text: PlayerText,
  private val lore: PlayerText? = null,
  private val onInteract: PlayerItemInteraction? = null,
  private val cooldownSeconds: Long = -1,
) {
  private val cooldowns: MutableMap<UUID, Cooldown> = mutableMapOf()

  fun renderForPlayer(player: Player): ItemStack {
    val itemStackBuilder = ItemStack.builder(material).amount(amount)

    itemStackBuilder.displayName(
      text.miniMessage(player).style(
        Style.empty().decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE),
      ),
    )

    lore?.let { nonNullLore ->
      val loreComponents: MutableList<Component> = ArrayList()
      val rawLoreLines = nonNullLore.string(player).split("\n")
      for (rawLoreLine in rawLoreLines) {
        loreComponents.add(
          rawLoreLine.miniMessage()
            .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE),
        )
      }
      itemStackBuilder.lore(loreComponents)
    }

    return itemStackBuilder.build()
  }

  fun onPlayerInteract(player: Player) {
    val currentCooldown = cooldowns[player.uuid]
    val currentTime = System.currentTimeMillis()

    if (cooldownSeconds > 0 && currentCooldown != null && !currentCooldown.isReady(currentTime)) {
      return
    }

    if (cooldownSeconds > 0 && currentCooldown == null) {
      cooldowns[player.uuid] = Cooldown(Duration.ofSeconds(cooldownSeconds))
    }

    currentCooldown?.refreshLastUpdate(currentTime)
    sendClientUpdateCooldown(player)
    (onInteract ?: NO_INTERACTION).interact(this, player)
  }

  private fun sendClientUpdateCooldown(player: Player) {
    if (cooldownSeconds > 0) {
      player.sendPacket(SetCooldownPacket(material.id(), (cooldownSeconds * 20).toInt()))
    }
  }

  fun interface PlayerItemInteraction {
    fun interact(
      item: MiniGameItem,
      player: Player,
    )
  }

  companion object {
    private val NO_INTERACTION = PlayerItemInteraction { _, _ -> }
  }
}
