package com.daylifecraft.minigames.metrics

import com.daylifecraft.common.metrics.MinestomMetrics
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import io.prometheus.metrics.model.registry.Collector
import io.prometheus.metrics.model.registry.PrometheusRegistry
import net.minestom.server.MinecraftServer
import net.minestom.server.event.GlobalEventHandler
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class MinestomMetricsTests {

  @ParameterizedTest
  @ValueSource(
    strings = [
      "online_players",
      "loaded_instances",
      "loaded_chunks",
      "tick_time_seconds",
      "acq_time_seconds",
      "packets_out",
      "packets_in",
      "ticks",
    ],
  )
  fun registrationTest(metricName: String) {
    mockkStatic(MinecraftServer::class) {
      val mockedEventHandler = mockk<GlobalEventHandler>(relaxed = true)
      every { mockedEventHandler.addListener(any()) } returns mockedEventHandler
      every { MinecraftServer.getGlobalEventHandler() } returns mockedEventHandler

      val registry = mockk<PrometheusRegistry>(relaxed = true)
      MinestomMetrics.builder().register(registry)

      verify {
        registry.register(
          match<Collector> {
            it.prometheusName == metricName
          },
        )
      }

      registry.scrape { true }
    }
  }
}
