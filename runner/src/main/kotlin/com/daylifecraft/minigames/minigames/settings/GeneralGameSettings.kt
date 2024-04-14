package com.daylifecraft.minigames.minigames.settings

import com.daylifecraft.common.config.ConfigFile
import com.daylifecraft.common.util.Range
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
 * @param playersCount Range of players count
 * @param groupSize Range of current group
 */
@JvmRecord
data class GeneralGameSettings(
  val name: String,
  val displayNameKey: String,
  val descriptionKey: String,
  val guiBlock: Material,
  val isPublic: Boolean,
  val permission: String?,
  val playersCount: Range<Int>,
  val groupSize: Range<Int>,
  val gameConfig: ConfigFile,
)
