package com.daylifecraft.common.util

import java.security.SecureRandom
import kotlin.random.asKotlinRandom

object RandomUtil {
  val SHARED_SECURE_RANDOM = SecureRandom().asKotlinRandom()
}
