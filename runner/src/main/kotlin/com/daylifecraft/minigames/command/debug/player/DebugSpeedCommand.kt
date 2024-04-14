package com.daylifecraft.minigames.command.debug.player

import com.daylifecraft.minigames.command.CommandsManager
import com.daylifecraft.minigames.command.debug.AbstractDebugCommand
import net.minestom.server.attribute.Attribute
import net.minestom.server.attribute.AttributeModifier
import net.minestom.server.attribute.AttributeOperation
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.arguments.ArgumentEnum
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.command.builder.exception.ArgumentSyntaxException
import net.minestom.server.entity.Player
import java.nio.charset.StandardCharsets
import java.util.UUID
import kotlin.math.max

/** Speed command (/~ speed fly/walk &lt;value&gt;) Command editing walk or fly speed  */
class DebugSpeedCommand : AbstractDebugCommand("speed") {
  private val typeArgument: ArgumentEnum<SpeedType>
  private val valueArgument = ArgumentType.Double("value")

  init {
    typeArgument =
      object : ArgumentEnum<SpeedType>("type", SpeedType::class.java) {
        override fun parse(sender: CommandSender, input: String): SpeedType {
          for (speedType in SpeedType.entries) {
            if (speedType.toString().equals(input, ignoreCase = true)) {
              return speedType
            }
          }

          CommandsManager.getSenderLanguage(sender)
            .sendMiniMessage("debug.speed.fail.wrong-type")
          throw ArgumentSyntaxException(
            "Not a " + SpeedType::class.java.simpleName + " value",
            input,
            1,
          )
        }
      }

    addSyntax(
      this::onExecute,
      typeArgument.setFormat(ArgumentEnum.Format.LOWER_CASED),
      valueArgument,
    )
  }

  override fun onCommandUse(sender: CommandSender, context: CommandContext) {
    val senderLanguage = CommandsManager.getSenderLanguage(sender)

    val value: Double = context[valueArgument]
    if (value < 0) {
      senderLanguage.sendMiniMessage("debug.speed.fail.wrong-value")
      return
    }

    val player = sender as Player
    val speedType: SpeedType = context[typeArgument]
    if (speedType == SpeedType.WALK) {
      if (value == 1.0) {
        player.getAttribute(Attribute.MOVEMENT_SPEED).removeModifier(ATTRIBUTE_MODIFIER_UUID)
        player.getAttribute(Attribute.FLYING_SPEED).removeModifier(ATTRIBUTE_MODIFIER_UUID)
      } else {
        val result = max(0.0, value) - 1

        player.getAttribute(Attribute.MOVEMENT_SPEED).removeModifier(ATTRIBUTE_MODIFIER_UUID)
        player
          .getAttribute(Attribute.MOVEMENT_SPEED)
          .addModifier(
            AttributeModifier(
              ATTRIBUTE_MODIFIER_UUID,
              ATTRIBUTE_MODIFIER_NAME,
              result,
              AttributeOperation.MULTIPLY_TOTAL,
            ),
          )

        player.getAttribute(Attribute.FLYING_SPEED).removeModifier(ATTRIBUTE_MODIFIER_UUID)
        player
          .getAttribute(Attribute.FLYING_SPEED)
          .addModifier(
            AttributeModifier(
              ATTRIBUTE_MODIFIER_UUID,
              ATTRIBUTE_MODIFIER_NAME,
              result,
              AttributeOperation.MULTIPLY_TOTAL,
            ),
          )
      }

      senderLanguage.sendMiniMessage(
        "debug.speed.walk.success",
        "walkSpeed" to player.getAttributeValue(Attribute.MOVEMENT_SPEED).toString(),
      )
    } else {
      player.flyingSpeed = (value * DEFAULT_FLY_SPEED).toFloat()

      senderLanguage.sendMiniMessage(
        "debug.speed.fly.success",
        "flySpeed" to player.flyingSpeed.toString(),
      )
    }
  }

  private enum class SpeedType {
    WALK,
    FLY,
  }

  companion object {
    /**
     * Attribute name for speed modifier (It`s used in walk speed modifying)
     */
    const val ATTRIBUTE_MODIFIER_NAME: String = "DEBUG_SPEED_COMMAND"

    /**
     * Attribute name
     */
    private val ATTRIBUTE_MODIFIER_UUID: UUID = UUID.nameUUIDFromBytes(ATTRIBUTE_MODIFIER_NAME.toByteArray(StandardCharsets.UTF_8))

    /**
     * Default player fly speed
     */
    private const val DEFAULT_FLY_SPEED = 0.05
  }
}
