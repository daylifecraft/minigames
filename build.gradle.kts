plugins {
  kotlin("jvm") version "1.9.23"
  jacoco
  id("io.gitlab.arturbosch.detekt") version "1.23.6"
  id("org.sonarqube") version "5.0.0.4638"
}

repositories {
  mavenCentral()
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
