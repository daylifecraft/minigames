package com.daylifecraft.minigames.minigames.settings

import com.daylifecraft.common.config.ConfigFile
import net.minestom.server.item.Material

/**
 * Basic settings of the mini-game
 *
 * @param name MiniGame ID key
 * @param displayNameKey DisplayName i18n key
 * @param descriptionKey Description i18n key
 * @param guiBlock Material to draw in GUI
 * @param isPublic is MiniGae public or not
 * @param permission Permission to join
 * @param playersCountRange Range of players count
 * @param groupSizeRange Range of current group
 */
data class GeneralGameSettings(
  val name: String,
  val displayNameKey: String,
  val descriptionKey: String,
  val guiBlock: Material,
  val isPublic: Boolean,
  val permission: String?,
  val playersCountRange: IntRange,
  val groupSizeRange: IntRange,
  val gameConfig: ConfigFile,
)
