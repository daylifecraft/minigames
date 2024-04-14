package com.daylifecraft.common.util

/**
 * Safely Converting an Object to a List
 *
 * @return list
 */
inline fun <reified T> Any?.safeCastToList(): List<T>? = (this as? List<*>)?.map { it as T }

/**
 * Safely Converting an Object to a List
 *
 * @return list
 */
@JvmName("safeCastToNotNullList")
inline fun <reified T> Any.safeCastToList(): List<T> = (this as List<*>).map { it as T }

/**
 * Safely Converting an Object to Array
 *
 * @return array
 */
inline fun <reified T> Any?.safeCastToArray(): Array<T>? = (this as? Array<*>)?.map { it as T }?.toTypedArray()

/**
 * Safely Converting an Object to Array
 *
 * @return array
 */
@JvmName("safeCastToNotNullArray")
inline fun <reified T> Any.safeCastToArray(): Array<T> = (this as Array<*>).map { it as T }.toTypedArray()
