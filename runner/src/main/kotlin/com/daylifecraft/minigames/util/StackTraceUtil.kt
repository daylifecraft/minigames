package com.daylifecraft.minigames.util

import com.daylifecraft.common.util.extensions.miniMessage
import com.daylifecraft.minigames.Dev
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.HoverEvent
import net.minestom.server.entity.Player
import java.text.SimpleDateFormat
import java.util.Locale

private const val STACK_TRACE_SIZE = 6
private const val ELLIPSIS_THRESHOLD = 7

private val SHORT_TIME_FORMAT = SimpleDateFormat("mm:ss:SSS", Locale.ENGLISH)
private val LONG_TIME_FORMAT = SimpleDateFormat("HH:mm:ss:SSS", Locale.ENGLISH)

/** Sends caller function stack trace to player as minimessage  */
fun sendStackTrace(player: Player, label: String? = null) {
  if (!Dev.isDev) return // TODO think about conditional compilation

  val stackTrace = Exception("dummy").stackTrace

  val firstNonDevClass = stackTrace.indexOfFirstNonDev()
  if (firstNonDevClass !in stackTrace.indices) return

  val stackTraceElement = stackTrace[firstNonDevClass]

  val appendix: Component =
    when {
      label == null -> Component.empty()
      label.length > ELLIPSIS_THRESHOLD -> "<red>: ...</red>".miniMessage()
      else -> "<gold>: $label</gold>".miniMessage()
    }

  val fullStackTraceComponent = Component.text()

  fullStackTraceComponent.append(
    when (label) {
      null -> Component.text("No-message!")
      else -> Component.text(label)
    },
  )

  for (i in firstNonDevClass..<firstNonDevClass + STACK_TRACE_SIZE) {
    if (i >= stackTrace.size) break
    fullStackTraceComponent.appendNewline().append(stackTrace[i].toChatComponent())
  }

  val finalMessage =
    buildFinalMessage(
      player,
      stackTraceElement.toFirstLineChatComponent().append(appendix),
      fullStackTraceComponent.build(),
    )

  player.sendMessage(finalMessage)
}

private fun buildFinalMessage(player: Player, mainMessage: Component, hover: Component): Component {
  val currTime = System.currentTimeMillis()
  val shortTime = SHORT_TIME_FORMAT.format(currTime)
  val longTime = LONG_TIME_FORMAT.format(currTime)

  val resultHover: Component =
    Component.text()
      .append(
        "<light_purple>[$longTime] p.inst=${player.instance?.uniqueId}</light_purple>"
          .miniMessage(),
      )
      .appendSpace()
      .append(mainMessage)
      .appendNewline()
      .appendNewline()
      .append(hover)
      .build()

  return "<dark_purple>></dark_purple><light_purple>[$shortTime]</light_purple>"
    .miniMessage()
    .appendSpace()
    .append(mainMessage)
    .hoverEvent(HoverEvent.showText(resultHover))
}

private fun StackTraceElement.toChatComponent(): Component = (
  "<dark_green>$fileName</dark_green>" +
    "<dark_red>::</dark_red><gold>$methodName</gold>" +
    "<dark_green>:$lineNumber</dark_green>"
  ).miniMessage()

private fun StackTraceElement.toFirstLineChatComponent(): Component = (
  "<green>✔</green><dark_green>${fileName?.replace(".java", "")}</dark_green>" +
    "<dark_red>::</dark_red><gold>$methodName</gold>" +
    "<dark_gray>:$lineNumber</dark_gray>"
  ).miniMessage()

private fun Array<out StackTraceElement>.indexOfFirstNonDev(): Int = indexOfFirst { it.fileName != "Dev.kt" }
