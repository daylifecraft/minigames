package com.daylifecraft.minigames.minigames.instances.games.towerdefence

import com.daylifecraft.common.util.RandomUtil
import com.daylifecraft.common.util.extensions.clamp
import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.Entity
import net.minestom.server.utils.math.DoubleRange
import kotlin.math.max
import kotlin.math.min

private val RANDOM = RandomUtil.SHARED_SECURE_RANDOM

class CuboidRegion(
  private val firstPoint: Point,
  private val secondPoint: Point,
) {

  fun getRandomPoint(): Point {
    val minimum = getMinimumPoint()
    val maximum = getMaximumPoint()

    return Vec(
      getRandomClampedValue(minimum.x(), maximum.x()),
      getRandomClampedValue(minimum.y(), maximum.y()),
      getRandomClampedValue(minimum.z(), maximum.z()),
    )
  }

  fun getRandomBlockPoint(): Point {
    val minimum = getMinimumPoint()
    val maximum = getMaximumPoint()

    return Vec(
      getRandomClampedValue(minimum.blockX(), maximum.blockX()).toDouble(),
      getRandomClampedValue(minimum.blockY(), maximum.blockY()).toDouble(),
      getRandomClampedValue(minimum.blockZ(), maximum.blockZ()).toDouble(),
    )
  }

  fun isInRegion(entity: Entity): Boolean = isInRegion(entity.position)

  fun isInRegion(point: Point): Boolean = isInRegion(point.x(), point.y(), point.z())

  fun isInRegion(x: Double, y: Double, z: Double): Boolean {
    val minimum = getMinimumPoint()
    val maximum = getMaximumPoint()

    return DoubleRange(minimum.x(), maximum.x()).isInRange(x) &&
      DoubleRange(minimum.y(), maximum.y()).isInRange(y) &&
      DoubleRange(minimum.z(), maximum.z()).isInRange(z)
  }

  private fun getMinimumPoint(): Point = Vec(
    min(firstPoint.x(), secondPoint.x()),
    min(firstPoint.y(), secondPoint.y()),
    min(firstPoint.z(), secondPoint.z()),
  )

  private fun getMaximumPoint(): Point = Vec(
    max(firstPoint.x(), secondPoint.x()),
    max(firstPoint.y(), secondPoint.y()),
    max(firstPoint.z(), secondPoint.z()),
  )

  private fun getRandomClampedValue(minimum: Double, maximum: Double): Double = clamp(RANDOM.nextDouble(minimum, maximum + 1), minimum.rangeTo(maximum))

  private fun getRandomClampedValue(minimum: Int, maximum: Int): Int = clamp(RANDOM.nextInt(minimum, maximum + 1), IntRange(minimum, maximum))

  private fun <T : Comparable<T>> clamp(value: T, range: ClosedRange<T>): T = range.clamp(value)
}
