package com.daylifecraft.minigames.command.punishment.give.ban

import com.daylifecraft.minigames.command.punishment.AbstractPunishmentManager
import com.daylifecraft.minigames.profile.punishment.PunishmentType
import com.daylifecraft.minigames.text.i18n.PlayerLanguage.Companion.get
import net.minestom.server.command.CommandSender
import net.minestom.server.entity.Player
import java.util.UUID

class BanManager : AbstractPunishmentManager() {
  public override fun showTemp(player: Player, totalEndTime: String) {
    kickPlayerWithLangMiniMessage(
      player,
      "user.temp-banned-kick",
      "banTotalExpireDate" to totalEndTime,
    )
  }

  public override fun showPerm(player: Player) {
    kickPlayerWithLangMiniMessage(player, "user.perm-banned-kick")
  }

  /**
   * This method will be called when the command was confirmed
   *
   * @param sender player to ban uuid
   * @param uuid sender
   * @param moderator moderator uuid
   * @param reason reason for ban
   * @param duration ban duration
   */
  public override fun give(
    uuid: UUID,
    sender: CommandSender,
    moderator: UUID,
    reason: String,
    duration: Long,
  ) {
    give(uuid, sender, moderator, reason, PunishmentType.BAN, "banned", duration)
  }

  companion object {
    private fun kickPlayerWithLangMiniMessage(
      player: Player,
      key: String,
      vararg variables: Pair<String, String?>,
    ) {
      player.kick(get(player).miniMessage(key, *variables))
    }
  }
}
