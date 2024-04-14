package com.daylifecraft.common.util

import com.google.gson.Gson
import com.google.gson.JsonObject

object JsonUtils {
  @JvmStatic
  fun getJsonObjectFromString(input: String): JsonObject = Gson().fromJson(input, JsonObject::class.java)
}
