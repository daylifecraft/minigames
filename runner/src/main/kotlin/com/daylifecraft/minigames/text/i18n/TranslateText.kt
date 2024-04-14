package com.daylifecraft.minigames.text.i18n

import com.daylifecraft.common.logging.building.createLogger
import com.daylifecraft.common.text.PlayerText
import net.minestom.server.entity.Player
import java.util.function.Supplier
import java.util.function.UnaryOperator

class TranslateText(
  private val key: String,
  private vararg val variables: Pair<String, Any?>,
) : PlayerText {
  private fun string(playerLanguage: PlayerLanguage): String {
    return playerLanguage.string(
      key,
      *Array(variables.size) {
        val variable = variables[it]
        return@Array Pair(
          first = variable.first,
          second = parseArgument(0, variable.second, variable.first, playerLanguage),
        )
      },
    )
  }

  private fun parseArgument(
    depth: Int,
    arg: Any?,
    lastKey: String?,
    playerLanguage: PlayerLanguage,
  ): String? {
    if (depth > DEPTH_LIMIT) {
      LOGGER.debug("WARN: TranslateText::parseArgument recursion depth limit reached. depth=$depth")
      return ARGUMENT_PARSE_ERROR_UNARY.apply("[!]Recursion depth limit reached: $depth")
    }

    return when (arg) {
      is String -> arg

      // TODO two branches below are identical in kotlin
      is Function0<*> -> {
        val ret: Any = arg.invoke()!!

        if (ret === arg) {
          LOGGER.debug("WARN: recursion detected in Function0=$arg; key=$key")
          (ARGUMENT_PARSE_ERROR_UNARY.apply("[!]recursion in Function0..."))
        } else {
          (parseArgument(depth + 1, ret, lastKey, playerLanguage))
        }
      }

      is Supplier<*> -> {
        val ret: Any = arg.get()
        if (ret === arg) {
          LOGGER.debug("WARN: recursion detected in Supplier=$arg; key=$key")
          ARGUMENT_PARSE_ERROR_UNARY.apply("[!]recursion in Supplier...")
        } else {
          parseArgument(depth + 1, ret, lastKey, playerLanguage)
        }
      }

      is PlayerText -> {
        if (arg !== this) {
          arg.string(playerLanguage.player)
        } else {
          LOGGER.debug("WARN: recursion detected in PlayerText=$arg; key=$key")
          ARGUMENT_PARSE_ERROR_UNARY.apply("[!]recursion in PlayerText's...")
        }
      }

      else -> {
        LOGGER.debug(
          "WARN: unknown argument type for lastKey=$lastKey" +
            "; argType = ${arg?.javaClass?.name}",
        )

        ARGUMENT_PARSE_ERROR_UNARY.apply(
          "([!]unknown argument type for key '$lastKey': ${arg?.javaClass?.name})",
        )
      }
    }
  }

  override fun string(player: Player): String = string(PlayerLanguage.get(player))

  companion object {
    private const val DEPTH_LIMIT: Long = 10
    private val ARGUMENT_PARSE_ERROR_UNARY: UnaryOperator<String> = UnaryOperator.identity()
    private val LOGGER = createLogger<TranslateText>()
  }
}
