package com.daylifecraft.common.util

import net.minestom.server.entity.Player
import java.net.Inet6Address
import java.net.InetSocketAddress

object NetworkUtil {
  private fun compressIpv6Address(source: String): String = source
    .replace("((?::0\\b){2,}):?(?!\\S*\\b\\1:0\\b)(\\S*)".toRegex(), "::$2")
    .replaceFirst("^0::".toRegex(), "::")

  /**
   * Gets player ip
   *
   * @param socketAddress socket address of player
   * @return player ip address
   */
  fun getPlayerIp(socketAddress: InetSocketAddress): String {
    val inetAddress = socketAddress.address
    if (inetAddress is Inet6Address) {
      return compressIpv6Address(inetAddress.getHostAddress())
    }
    return inetAddress.hostAddress
  }

  /**
   * Gets player socket address
   *
   * @param player the player instance from which we need to get the IP
   * @return player socket address
   */
  fun getPlayerAddress(player: Player): InetSocketAddress {
    val connection = player.playerConnection
    val remoteAddress = connection.remoteAddress
    if (remoteAddress is InetSocketAddress) {
      return remoteAddress
    }
    throw IllegalArgumentException("Cannot cast Player SocketAddress to InetSocketAddress")
  }
}
