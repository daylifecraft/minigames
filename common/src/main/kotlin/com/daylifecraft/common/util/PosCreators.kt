package com.daylifecraft.common.util

import net.minestom.server.coordinate.Pos

fun posOf(
  x: Double = 0.0,
  y: Double = 0.0,
  z: Double = 0.0,
  yaw: Float = 0f,
  pitch: Float = 0f,
): Pos = Pos(x, y, z, yaw, pitch)
