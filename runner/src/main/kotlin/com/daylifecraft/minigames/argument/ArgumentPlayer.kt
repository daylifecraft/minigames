package com.daylifecraft.minigames.argument

import com.daylifecraft.common.finder.PlayerFinder
import net.minestom.server.MinecraftServer
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.arguments.Argument
import net.minestom.server.command.builder.exception.ArgumentSyntaxException
import net.minestom.server.utils.binary.BinaryWriter

open class ArgumentPlayer internal constructor(id: String) : Argument<PlayerFinder>(id) {
  private var excludeSamePlayer = false

  /**
   * If true, argument can't be same player
   *
   * @param value should the same player be excluded?
   */
  fun excludeSamePlayer(value: Boolean) {
    excludeSamePlayer = value
  }

  /**
   * Parse sender and input(username) into PlayerFinder object
   *
   * @param sender sender
   * @param input username
   */
  @Throws(ArgumentSyntaxException::class)
  override fun parse(sender: CommandSender, input: String): PlayerFinder {
    val player = MinecraftServer.getConnectionManager().getOnlinePlayerByUsername(input)

    if (player == null || excludeSamePlayer && sender == player) {
      return PlayerFinder(input, true)
    }

    return PlayerFinder(player, input)
  }

  override fun nodeProperties(): ByteArray = BinaryWriter.makeArray { packetWriter: BinaryWriter -> packetWriter.writeVarInt(0) }

  override fun parser(): String = "brigadier:string"
}
