plugins {
  kotlin("jvm")
  `jvm-test-suite`
  jacoco
  id("org.sonarqube")
  id("io.gitlab.arturbosch.detekt")
}

repositories {
  mavenCentral()
  maven(url = "https://jitpack.io")
}

dependencies {
  implementation(project(":common"))
}

kotlin {
  jvmToolchain(21)
  compilerOptions {
    freeCompilerArgs.add("-Xjvm-default=all")
    allWarningsAsErrors = true
    verbose = true
  }
}

tasks.register<Copy>("copyDependenciesToLibs") {
  from(configurations.runtimeClasspath)
  into("build/libs/dependencies")
}

tasks.processResources {
  include("META-INF/**/*")
}

tasks.register<Copy>("copyResourcesToLibs") {
  from("src/main/resources")
  into("build/libs/resources")
}

tasks.jar {
  dependsOn("copyDependenciesToLibs")
  dependsOn("copyResourcesToLibs")
  manifest.attributes["Main-Class"] = "com.daylifecraft.minigames.Init"
  manifest.attributes["Class-Path"] = configurations.runtimeClasspath.get().joinToString(" ") { "dependencies/${it.name}" }
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
  mustRunAfter(":runner:integration-tests")
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
            systemProperty("junit.jupiter.extensions.autodetection.enabled", true)
            maxParallelForks = Runtime.getRuntime().availableProcessors().coerceAtLeast(1)
          }
        }
      }
      dependencies {
        implementation(project())
        implementation(project(":common"))
        implementation(kotlin("test"))
        implementation("io.mockk:mockk:1.13.10")
        implementation("org.mockito:mockito-core:5.11.0")
        implementation("org.mockito:mockito-junit-jupiter:5.11.0")
      }
    }

    register<JvmTestSuite>("integration-tests") {
      targets {
        all {
          testTask.configure {
            maxParallelForks = 1
          }
        }
      }
    }
  }
}

tasks.named("check") {
  dependsOn(testing.suites.named("integration-tests"))
}
