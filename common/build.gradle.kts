plugins {
  kotlin("jvm") version "1.9.23"
  jacoco
  id("org.sonarqube") version "5.0.0.4638"
  id("io.gitlab.arturbosch.detekt") version "1.23.6"
}

repositories {
  mavenCentral()
  maven(url = "https://jitpack.io")
}

dependencies {
  api(kotlin("stdlib-jdk8"))
  api("net.minestom:minestom-snapshots:7320437640")

  api("io.prometheus:prometheus-metrics-core:1.2.1")
  api("io.prometheus:prometheus-metrics-instrumentation-jvm:1.2.1")
  api("io.prometheus:prometheus-metrics-exporter-httpserver:1.2.1")

  api("org.snakeyaml:snakeyaml-engine:2.7")
  api("net.kyori:adventure-text-minimessage:4.16.0")
  api("org.mongodb:mongodb-driver-sync:5.0.1")
}

kotlin {
  jvmToolchain(21)
  compilerOptions {
    allWarningsAsErrors = true
    verbose = true
  }
}

detekt {
  parallel = true
  buildUponDefaultConfig = true
  allRules = true
  ignoreFailures = true
  config.setFrom("$rootDir/.github/detekt-config.yml")
}

tasks.jacocoTestReport {
  executionData.setFrom(fileTree(layout.buildDirectory).include("/jacoco/*.exec"))
  reports {
    xml.required.set(true)
    csv.required.set(false)
    html.required.set(false)
  }
}

tasks.test {
  finalizedBy("jacocoTestReport")
}

testing {
  suites {
    fun JvmComponentDependencies.kotlin(module: String) =
      project.dependencies.kotlin(module) as String

    withType<JvmTestSuite> {
      useJUnitJupiter("5.10.1")
      targets {
        all {
          testTask.configure {
            testLogging {
              events("passed", "skipped", "failed")
            }
            reports.html.required.set(false)
            maxParallelForks = Runtime.getRuntime().availableProcessors().coerceAtLeast(1)
          }
        }
      }

      dependencies {
        implementation(kotlin("test"))
        implementation("io.mockk:mockk:1.13.10")
        implementation("org.mockito:mockito-core:5.11.0")
        implementation("org.mockito.kotlin:mockito-kotlin:5.3.1")
        implementation("org.mockito:mockito-junit-jupiter:5.11.0")
      }
    }
  }
}
