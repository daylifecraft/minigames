package com.daylifecraft.minigames.config

data class MainConfig(
  val collections: List<String>,
  val roundInvitationTimeout: Long,
  val roundSearchTime: Long,
  val chat: ChatConfig,
  val groups: List<GroupConfig>,
  val ports: PortsConfig,
  val seasons: List<SeasonConfig>,
)

data class ChatConfig(
  val global: GlobalChatConfig,
  val group: GroupChatConfig,
  val privateMessages: PrivateMessagesChatConfig,
  val spectator: SpectatorChatConfig,
)

data class GlobalChatConfig(
  val pattern: String,
)

data class GroupChatConfig(
  val membersPattern: String,
  val leaderPattern: String,
)

data class PrivateMessagesChatConfig(
  val fromDefaultPattern: String,
  val toDefaultPattern: String,

  val fromAdminPattern: String,
  val toAdminPattern: String,
)

data class SpectatorChatConfig(
  val pattern: String,
)

data class GroupConfig(
  val name: String,
  val badge: String,
  val globalChatColor: String?,
  val permissions: List<String>,
)

data class PortsConfig(
  val minecraft: Int,
  val prometheus: Int,
)

data class SeasonConfig(
  val name: String,
  val displayName: String,
  val startDate: String,
  val endDate: String,
)
