package com.daylifecraft.minigames.command.group.invites

import net.minestom.server.entity.Player

/**
 * @property inviter player that created this invite
 * @property target player who is invited
 * @property onAccept callback that will be called when invite will be accepted
 */
data class GroupInvite(
  val inviter: Player,
  val target: Player,
  val onAccept: () -> Unit,
)
