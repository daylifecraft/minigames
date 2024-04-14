package com.daylifecraft.minigames.util

import com.daylifecraft.common.logging.building.createLogger
import com.daylifecraft.common.logging.foundation.LogEvent
import com.daylifecraft.minigames.Init.isInsideTests
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.net.URISyntaxException
import java.net.URL
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/** Utility class to work with files. It includes Resources-utilities  */
object FilesUtil {
  private const val JAR_SCHEME = "jar"

  private val LOGGER = createLogger()

  private val FILES_PREFIX: String by lazy {
    val currentClassName = FilesUtil::class.java.canonicalName.replace('.', '/')
    val classPath = FilesUtil::class.java.getResource("/$currentClassName.class")

    return@lazy when {
      isInsideTests -> TESTS_RESOURCES_PREFIX

      classPath === null -> {
        LOGGER.build(LogEvent.SERVER_CRASHED) {
          message("Unable to load class path to FilesUtils.class")
          details("currentClassName", currentClassName)
          details("classPath", null)
        }
        FILE_RESOURCES_PREFIX
      }

      classPath.toString().startsWith(JAR_SCHEME) -> JAR_RESOURCES_PREFIX

      else -> FILE_RESOURCES_PREFIX
    }
  }

  private const val JAR_RESOURCES_PREFIX = "resources"
  private const val FILE_RESOURCES_PREFIX = "src/main/resources/"
  private const val TESTS_RESOURCES_PREFIX = "src/integration-tests/resources/"

  private fun walkThrowResourcesDir(folderPath: Path): Collection<Path> {
    val folderName = folderPath.getName(folderPath.nameCount - 1).toString()

    try {
      Files.walk(folderPath, 1).use { stream ->
        return stream // Exclude folder path from files list
          .filter { path: Path -> folderName != path.fileName.toString() }
          .toList()
      }
    } catch (exception: Exception) {
      LOGGER.build(LogEvent.SERVER_CRASHED) {
        message("Some error while reading resources folder $exception")
      }
      return emptyList()
    }
  }

  /**
   * Gets a path object for a given path (string)
   *
   * @param path the path we need
   * @return path as Path object
   */
  fun getResourcesPath(path: String): Path = File(FILES_PREFIX + File.separator + path).toPath()

  /**
   * Iterates through the files inside specified resources folder.
   *
   * @param folderName Relative resource-folder path
   * @return a collection of files in the specified directory
   * @throws IllegalArgumentException if directory does not found
   */
  fun walkThrowResourcesDir(folderName: String): Collection<Path> {
    val file = File(FILES_PREFIX + File.separator + folderName)
    return walkThrowResourcesDir(file.toPath())
  }

  /**
   * Iterates through the files inside specified resources folder.
   *
   * @param folderName Relative resource-folder path
   * @return a collection of files in the specified directory
   * @throws IllegalArgumentException if directory does not found
   * @throws IOException if an I/O error is thrown when accessing the folder
   * @throws URISyntaxException Bad URL syntax
   */
  @Throws(IOException::class, URISyntaxException::class)
  fun walkThrowClasses(folderName: String): Collection<Path> {
    val resourceUrl =
      getResourceUrlByPath(folderName)
        ?: return emptyList()

    val folderUri = resourceUrl.toURI()

    // Different ways to get path from jar or standard file system (IDE)
    if (JAR_SCHEME == folderUri.scheme) {
      FileSystems.newFileSystem(folderUri, emptyMap<String, Any>()).use { fileSystem ->
        return walkThrowResourcesDir(fileSystem.getPath(folderName))
      }
    } else {
      return walkThrowResourcesDir(Paths.get(folderUri))
    }
  }

  /**
   * Loading resource from path.
   *
   * @param path relative path to file
   * @return URL of specified file
   */
  private fun getResourceUrlByPath(path: String): URL? = Thread.currentThread().contextClassLoader.getResource(path)

  /**
   * Loads resource input stream by path.
   * Returns null when not found.
   *
   * @param path relative path to file
   * @return Loaded InputStream of specified file or null if not found
   */
  fun getResourceStreamByPathCatching(path: String): InputStream? {
    try {
      return getResourceStreamByPath(path)
    } catch (fileNotFound: FileNotFoundException) {
      LOGGER.build(LogEvent.GENERAL_DEBUG) {
        message("Cannot get InputStream by path: $path")
        details("stackTrace", fileNotFound.stackTrace)
        details("cause", fileNotFound.cause)
      }
      return null
    }
  }

  /**
   * Loading resource from path.
   *
   * @param path relative path to file
   * @return Loaded InputStream of specified file
   */
  fun getResourceStreamByPath(path: String): InputStream = FileInputStream(FILES_PREFIX + File.separator + path)
}
