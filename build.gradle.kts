plugins {
  kotlin("jvm") version "1.9.24"
  jacoco
  id("org.jetbrains.dokka") version "1.9.20"
  id("io.gitlab.arturbosch.detekt") version "1.23.6"
  id("org.sonarqube") version "5.0.0.4638"
}

repositories {
  mavenCentral()
}

subprojects {
  apply(plugin = "org.jetbrains.dokka")
}

detekt {
  parallel = true
  buildUponDefaultConfig = true
  allRules = true
  ignoreFailures = true
  config.setFrom("$rootDir/.github/detekt-config.yml")
}

sonar {
  properties {
    property("sonar.projectKey", "daylifecraft_minigames")
    property("sonar.organization", "daylifecraft")
    property("sonar.host.url", "https://sonarcloud.io")
  }
}
