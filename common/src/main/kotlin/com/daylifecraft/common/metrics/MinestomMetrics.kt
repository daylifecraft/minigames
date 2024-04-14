package com.daylifecraft.common.metrics

import com.daylifecraft.common.util.extensions.minestom.addListener
import io.prometheus.metrics.config.PrometheusProperties
import io.prometheus.metrics.core.metrics.Counter
import io.prometheus.metrics.core.metrics.Gauge
import io.prometheus.metrics.core.metrics.GaugeWithCallback
import io.prometheus.metrics.model.registry.PrometheusRegistry
import io.prometheus.metrics.model.snapshots.Unit
import net.minestom.server.MinecraftServer
import net.minestom.server.event.EventNode
import net.minestom.server.event.instance.InstanceChunkLoadEvent
import net.minestom.server.event.instance.InstanceChunkUnloadEvent
import net.minestom.server.event.player.PlayerPacketEvent
import net.minestom.server.event.player.PlayerPacketOutEvent
import net.minestom.server.event.server.ServerTickMonitorEvent
import java.util.Objects
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit

/**
 * Minestom specific metrics.
 *
 * Exported metrics:
 * - online_players (gauge)
 * - loaded_instances (gauge)
 * - loaded_chunks (gauge)
 * - tick_time_seconds (gauge)
 * - acq_time_seconds (gauge)
 * - packets_out_total (counter)
 * - packets_in_total (counter)
 * - ticks_total (counter)
 */
class MinestomMetrics private constructor(
  private val config: PrometheusProperties,
) {

  private fun register(registry: PrometheusRegistry) {
    GaugeWithCallback.builder(config)
      .name("online_players")
      .help("online players")
      .callback { it.call(MinecraftServer.getConnectionManager().onlinePlayerCount.toDouble()) }
      .register(registry)

    GaugeWithCallback.builder(config)
      .name("loaded_instances")
      .help("loaded instances")
      .callback { it.call(MinecraftServer.getInstanceManager().instances.size.toDouble()) }
      .register(registry)

    val loadedChunks = Gauge.builder(config)
      .name("loaded_chunks")
      .help("loaded chunks")
      .register(registry)

    val tickTime = Gauge.builder(config)
      .name("tick_time")
      .help("tick time in seconds")
      .unit(Unit.SECONDS)
      .register(registry)

    val acquisitionTime = Gauge.builder(config)
      .name("acq_time")
      .help("acquisition time in seconds")
      .unit(Unit.SECONDS)
      .register(registry)

    val packetsOut = Counter.builder(config)
      .name("packets_out")
      .help("packets sent by server")
      .register(registry)

    val packetsIn = Counter.builder(config)
      .name("packets_in")
      .help("packets received by server")
      .register(registry)

    val ticksCount = Counter.builder(config)
      .name("ticks")
      .help("ticks passed from server start")
      .register(registry)

    val events = EventNode.all(Objects.toIdentityString(this))
      .addListener { event: ServerTickMonitorEvent ->
        val monitor = event.tickMonitor
        tickTime.set(monitor.tickTime.milliseconds.toDouble(DurationUnit.SECONDS))
        acquisitionTime.set(monitor.acquisitionTime.milliseconds.toDouble(DurationUnit.SECONDS))

        ticksCount.inc()
      }
      .addListener { _: InstanceChunkLoadEvent ->
        loadedChunks.inc()
      }
      .addListener { _: InstanceChunkUnloadEvent ->
        loadedChunks.dec()
      }
      .addListener { _: PlayerPacketOutEvent ->
        packetsOut.inc()
      }
      .addListener { _: PlayerPacketEvent ->
        packetsIn.inc()
      }

    MinecraftServer.getGlobalEventHandler().addChild(events)
  }

  class Builder(private val config: PrometheusProperties) {
    /**
     * Adds all inner metrics to provided [registry].
     */
    fun register(registry: PrometheusRegistry = PrometheusRegistry.defaultRegistry) {
      MinestomMetrics(config).register(registry)
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
