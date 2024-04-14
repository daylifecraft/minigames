package com.daylifecraft.common.util.extensions

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage

fun String.miniMessage(): Component = MiniMessage.miniMessage().deserialize(this)
