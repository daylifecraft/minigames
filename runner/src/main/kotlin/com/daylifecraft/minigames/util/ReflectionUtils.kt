package com.daylifecraft.minigames.util

import com.daylifecraft.common.logging.building.createLogger
import com.daylifecraft.common.logging.foundation.LogEvent
import java.io.IOException
import java.net.URISyntaxException
import java.nio.file.Path
import java.util.Objects
import java.util.regex.Pattern

/** Class provides utils to work with Java Reflection: Loading class from package  */
object ReflectionUtils {
  private val LOGGER = createLogger()

  private val REPLACE_IN_PATH: Pattern = Pattern.compile("[.]")

  /**
   * Loading classes by package absolute name
   *
   * @param packageName absolute dotted package path
   * @param classParent Class parent to convert
   * @return Set of loaded classes or empty
   * @param <T> Type of parent class
   </T> */
  fun <T> getLoadedConvertedClassesFromPackage(packageName: String, classParent: Class<T>): Set<Class<out T>> {
    val convertedClasses: MutableSet<Class<out T>> = HashSet()

    for (clazz in getLoadedClassesFromPackage(packageName)) {
      if (!classParent.isAssignableFrom(clazz)) {
        continue
      }

      convertedClasses.add(clazz.asSubclass(classParent))
    }

    return convertedClasses
  }

  /**
   * Loading classes by package absolute name with unknown parent
   *
   * @param packageName absolute dotted package path
   * @return Set of loaded classes or empty
   */
  fun getLoadedClassesFromPackage(packageName: String): Set<Class<*>> {
    val normalPackageName = REPLACE_IN_PATH.matcher(packageName).replaceAll("/")
    try {
      return FilesUtil.walkThrowClasses(normalPackageName)
        .asSequence()
        .filter { path: Path -> path.toString().endsWith(".class") }
        .map { className: Path -> getClass(packageName, className.getName(className.nameCount - 1).toString()) }
        .filter(Objects::nonNull)
        .map { it!! }
        .toSet()
    } catch (exception: IOException) {
      LOGGER.build(LogEvent.GENERAL_DEBUG) {
        message("Unable to get classes from package: $packageName")
        details("stackTrace", exception.stackTrace)
        details("cause", exception.cause)
      }
      return emptySet()
    } catch (exception: URISyntaxException) {
      LOGGER.build(LogEvent.GENERAL_DEBUG) {
        message("Unable to get classes from package: $packageName")
        details("stackTrace", exception.stackTrace)
        details("cause", exception.cause)
      }
      return emptySet()
    }
  }

  /**
   * Loads class with specified name and class
   *
   * @param dottedPackageName dotted package path
   * @param className ClassName with .class ending
   * @return Loaded class
   */
  private fun getClass(dottedPackageName: String, className: String): Class<*>? {
    try {
      return Class.forName(
        dottedPackageName + "." + className.substring(0, className.lastIndexOf('.')),
      )
    } catch (e: ClassNotFoundException) {
      LOGGER.debug("Class not found exception ${e.message}")
    }
    return null
  }
}
