package com.daylifecraft.minigames.metrics

import com.daylifecraft.minigames.minigames.PlayerMiniGameManager
import io.prometheus.metrics.config.PrometheusProperties
import io.prometheus.metrics.core.metrics.GaugeWithCallback
import io.prometheus.metrics.model.registry.PrometheusRegistry

/**
 * Runner specific metrics.
 *
 * Exported metrics:
 * - active_rounds gauge
 */
class RunnerMetrics private constructor(
  private val config: PrometheusProperties,
) {
  private fun register(registry: PrometheusRegistry) {
    GaugeWithCallback.builder(config)
      .name("active_rounds")
      .help("active rounds")
      .callback { it.call(PlayerMiniGameManager.activeMiniGameInstances.size.toDouble()) }
      .register(registry)
  }

  class Builder(private val config: PrometheusProperties) {
    /**
     * Adds all inner metrics to provided [registry].
     */
    fun register(registry: PrometheusRegistry = PrometheusRegistry.defaultRegistry) {
      RunnerMetrics(config).register(registry)
    }
  }

  companion object {
    /**
     * Creates a builder of minestom metrics.
     *
     * Call of this method will have no effect without calling [Builder.register]
     *
     * @param config to be passed to all metrics builders
     */
    fun builder(
      config: PrometheusProperties = PrometheusProperties.get(),
    ) = Builder(config)
  }
}
