package com.daylifecraft.common.logging.building

import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

/** Creates logger for class on which this function was called.  */
inline fun <reified T> T.createLogger(): Logger = createLogger(T::class.java)

/** Creates logger for T::class  */
inline fun <reified T> createLogger(): Logger = createLogger(T::class.java)

/** Creates logger for name  */
fun createLogger(name: String): Logger = Slf4jLoggerWrapper(LoggerFactory.getLogger(name))

/** Creates logger for java class  */
fun createLogger(clazz: Class<*>): Logger = Slf4jLoggerWrapper(LoggerFactory.getLogger(clazz))

/** Creates logger for kotlin class  */
fun createLogger(clazz: KClass<*>): Logger = Slf4jLoggerWrapper(LoggerFactory.getLogger(clazz.java))
