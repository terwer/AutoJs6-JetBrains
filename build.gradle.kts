import org.gradle.api.tasks.JavaExec

plugins {
    id("org.jetbrains.intellij.platform") version "2.2.1"
    kotlin("jvm") version "2.0.21"
}

group = "org.autojs.autojs6"
version = "0.1.0"

repositories {
    mavenCentral()
    intellijPlatform { defaultRepositories() }
}

dependencies {
    intellijPlatform {
        create("IC", "2024.2")
        pluginVerifier()
        zipSigner()
    }
    testImplementation(kotlin("test"))
}

intellijPlatform {
    buildSearchableOptions.set(false)
}

kotlin { jvmToolchain(21) }

tasks {
    patchPluginXml {
        sinceBuild.set("242")
    }
    withType<JavaExec>().configureEach {
        if (name == "runIde" || name == "buildSearchableOptions") {
            systemProperty("gradle.compatibility.config.url", "")
            systemProperty("gradle.compatibility.update.interval", "0")
        }
    }
    test { useJUnitPlatform() }
}
