package com.daylifecraft.common.config

import com.daylifecraft.common.config.providers.Provider
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.jvmErasure

inline fun <reified T : Any> load(
  firstLoader: Provider,
  vararg loaders: Provider,
  basePath: ConfigPath = ConfigPath(),
): T {
  val l = mutableListOf(firstLoader)
  l.addAll(loaders)
  return load(T::class, basePath, l)
}

fun <T : Any> load(
  kClass: KClass<T>,
  basePath: ConfigPath,
  loaders: List<Provider>,
): T {
  if (!kClass.isData) throw IllegalArgumentException("T must be data class, but was ${kClass.simpleName}")

  val primaryConstructor = kClass.primaryConstructor ?: throw IllegalArgumentException("Primary constructor of data class not found!?")

  val arguments = mutableMapOf<KParameter, Any?>()

  for (constructorParameter in primaryConstructor.parameters) {
    basePath.addLast(StringToken(constructorParameter.name!!))

    val res = when (constructorParameter.type.jvmErasure) {
      Int::class -> loaders.tryGet { int(basePath) }

      Long::class -> loaders.tryGet { long(basePath) }

      Float::class -> loaders.tryGet { float(basePath) }

      Double::class -> loaders.tryGet { double(basePath) }

      Boolean::class -> loaders.tryGet { boolean(basePath) }

      String::class -> loaders.tryGet { string(basePath) }

      List::class -> {
        loadList(loaders, basePath, constructorParameter.type.arguments[0].type!!.jvmErasure)
      }

      else -> load(constructorParameter.type.jvmErasure, basePath, loaders)
    }
    basePath.removeLast()

    if (res == null) {
      if (constructorParameter.isOptional) continue
      if (!constructorParameter.type.isMarkedNullable) error("Not found argument for non-nullable constructor parameter: ${constructorParameter.name} On path: $basePath")
    }

    arguments[constructorParameter] = res
  }

  try {
    return primaryConstructor.callBy(arguments)
  } catch (e: Exception) {
    throw RuntimeException("Failed to instantiate class ${kClass.simpleName}. On path: $basePath", e)
  }
}

private fun loadList(
  loaders: List<Provider>,
  basePath: ConfigPath,
  listElementType: KClass<*>,
): List<Any> {
  val listSize = loaders.tryGet { listSize(basePath) } ?: 0
  return when (listElementType) {
    String::class -> loadList(listSize, basePath) {
      loaders.tryGet { string(basePath) }
    }

    Int::class -> loadList(listSize, basePath) {
      loaders.tryGet { int(basePath) }
    }

    else -> loadList(listSize, basePath) { path ->
      load(listElementType, path, loaders)
    }
  }
}

private inline fun loadList(
  listSize: Int,
  basePath: ConfigPath,
  getter: (ConfigPath) -> Any?,
): List<Any> = MutableList(listSize) {
  basePath.addLast(IntToken(it))
  val got = getter(basePath)
  basePath.removeLast()
  return@MutableList got ?: error("List element not found")
}

inline fun <T> List<Provider>.tryGet(block: Provider.() -> T): T? {
  for (loader in this) {
    val res = block.invoke(loader)
    if (res != null) return res
  }
  return null
}

data class Cfg(
  val cfgA: Int,
  val cfgB: String,
  val cfgC: C,
  val cfgD: Int? = null,
  val cfgE: Int?,
  val cfgF: Int = 1,
)

data class C(
  val a: Int,
  val b: String,
  val li: List<Int>,
  val ls: List<String>,
  val lo: List<D>,
)

data class D(
  val v: Int,
  val w: String,
  val cfg: Cfg,
)
