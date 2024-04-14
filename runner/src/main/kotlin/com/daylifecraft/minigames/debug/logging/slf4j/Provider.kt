package com.daylifecraft.minigames.debug.logging.slf4j

import org.slf4j.ILoggerFactory
import org.slf4j.IMarkerFactory
import org.slf4j.helpers.BasicMDCAdapter
import org.slf4j.helpers.BasicMarkerFactory
import org.slf4j.spi.MDCAdapter
import org.slf4j.spi.SLF4JServiceProvider

class Provider : SLF4JServiceProvider {

  private val loggerFactory: ILoggerFactory = LoggerFactory()
  private val markerFactory: IMarkerFactory = BasicMarkerFactory()
  private val mdcAdapter: MDCAdapter = BasicMDCAdapter()

  override fun getLoggerFactory(): ILoggerFactory = loggerFactory

  override fun getMarkerFactory(): IMarkerFactory = markerFactory

  override fun getMDCAdapter(): MDCAdapter = mdcAdapter

  override fun getRequestedApiVersion(): String = REQUESTED_API_VERSION

  override fun initialize() {
    // do nothing
  }

  companion object {
    private const val REQUESTED_API_VERSION = "2.0.99"
  }
}
