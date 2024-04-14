package com.daylifecraft.minigames.command.debug.server

import com.daylifecraft.common.command.SubCommand
import com.daylifecraft.common.util.extensions.minestom.scheduleTask
import com.daylifecraft.minigames.command.CommandsManager
import com.daylifecraft.minigames.command.debug.AbstractDebugCommand
import com.daylifecraft.minigames.text.i18n.Language
import net.minestom.server.MinecraftServer
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.timer.ExecutionType
import net.minestom.server.timer.Task
import net.minestom.server.timer.TaskSchedule
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/** '/~ lag <delay>' command executor  */
class DebugLagCommand :
  AbstractDebugCommand("lag"),
  SubCommand {

  private val delayArgument = ArgumentType.Double("delay")

  private val laggingTasks = mutableListOf<Task>()
  private var lagDelay: Duration? = null

  init {
    addSyntax(this::onExecute, delayArgument)
  }

  override fun onCommandUse(sender: CommandSender, context: CommandContext) {
    val senderLanguage = CommandsManager.getSenderLanguage(sender)

    val delay = context[delayArgument]

    if (delay == null) {
      senderLanguage.sendFailArgNotFound()
      return
    }

    val delayDuration = delay.milliseconds

    if (delayDuration.isNegative()) {
      senderLanguage.sendFailWrongValue()
      return
    }

    if (delayDuration == lagDelay) {
      senderLanguage.sendFailSameValue(delay)
      return
    }

    if (delayDuration == Duration.ZERO) {
      stopLags()
      senderLanguage.sendSuccessDisable()
    } else {
      startLags(delayDuration)
      senderLanguage.sendSuccess(delay)
    }
  }

  private fun startLags(delay: Duration) {
    stopLags()

    lagDelay = delay

    fun laggingTask() {
      TimeUnit.NANOSECONDS.sleep(delay.inWholeNanoseconds)
    }

    fun scheduleLaggingTask(executionType: ExecutionType) {
      laggingTasks.add(
        MinecraftServer.getSchedulerManager().scheduleTask(
          delay = TaskSchedule.tick(1),
          repeat = TaskSchedule.tick(1),
          executionType = executionType,
          task = ::laggingTask,
        ),
      )
    }

    scheduleLaggingTask(ExecutionType.TICK_START)
  }

  private fun stopLags() {
    lagDelay = null
    laggingTasks.forEach(Task::cancel)
    laggingTasks.clear()
  }
}

private fun Language.sendFailArgNotFound() =
  sendMiniMessage("commands.reusable.fails.required-args-not-found")
private fun Language.sendSuccess(delay: Double) =
  sendMiniMessage(
    "debug.lag.tick.success",
    "delayTime" to delay.toString(),
  )
private fun Language.sendSuccessDisable() =
  sendMiniMessage("debug.lag.tick.disable.success")
private fun Language.sendFailWrongValue() =
  sendMiniMessage("debug.lag.tick.fail.wrong-value")
private fun Language.sendFailSameValue(delay: Double) =
  sendMiniMessage(
    "debug.lag.tick.fail.same-value",
    "delayTime" to delay.toString(),
  )
