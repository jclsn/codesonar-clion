import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    kotlin("jvm") version "2.4.21"
    id("org.jetbrains.intellij.platform") version "2.20.1"
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

repositories {
    mavenCentral()

    intellijPlatform {
        defaultRepositories()
    }
}

// An installed CLion, instead of downloading one.
val clionPath = providers.gradleProperty("clionPath")
    .orElse(providers.environmentVariable("CLION_HOME"))

dependencies {
    intellijPlatform {
        if (clionPath.isPresent) {
            local(clionPath.get())
        } else {
            clion(providers.gradleProperty("platformVersion"))
        }

        testFramework(TestFrameworkType.Platform)
    }

    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter:6.1.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    // The test framework of the platform is based on JUnit 4.
    testImplementation("junit:junit:4.13.2")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:6.1.3")
}

intellijPlatform {
    // The plugin has no forms and no Java sources to instrument.
    instrumentCode = false
    buildSearchableOptions = false

    pluginConfiguration {
        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild")
            untilBuild = provider { null }
        }
    }
}

tasks {
    test {
        useJUnitPlatform()
        // The protocol tests run against this server, see the README.
        val server = providers.environmentVariable("CODESONAR_LSP").orElse("")
        inputs.property("codesonarLsp", server)
        // The daemon may have been started without it.
        doFirst { environment("CODESONAR_LSP", server.get()) }
    }
}
