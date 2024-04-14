package com.daylifecraft.common.util.extensions.minestom

import com.daylifecraft.common.util.extensions.miniMessage
import net.minestom.server.command.CommandSender

/**
 * Sends minimessage.
 *
 * Takes string in format of minimessage,
 * creates chat component out of it, and sends it to the given command sender.
 *
 * @param message string in minimessage format.
 */
fun CommandSender.sendMiniMessage(message: String) {
  sendMessage(message.miniMessage())
}
