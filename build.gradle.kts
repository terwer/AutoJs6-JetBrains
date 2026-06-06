import org.gradle.api.tasks.JavaExec

plugins {
    id("org.jetbrains.intellij.platform") version "2.2.1"
    kotlin("jvm") version "2.0.21"
}

group = "org.autojs.autojs6"
version = "0.1.1"

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
    signing {
        // Values are intentionally read from local environment variables only.
        // Do not commit signing keys, certificate chains, passwords, or Marketplace tokens.
        certificateChain.set(providers.environmentVariable("INTELLIJ_SIGN_CERTIFICATE_CHAIN"))
        privateKey.set(providers.environmentVariable("INTELLIJ_SIGN_PRIVATE_KEY"))
        password.set(providers.environmentVariable("INTELLIJ_SIGN_PRIVATE_KEY_PASSWORD"))
    }
    publishing {
        token.set(providers.environmentVariable("INTELLIJ_PLATFORM_PUBLISH_TOKEN"))
    }
    pluginVerification {
        ides {
            ide("IC", "2024.2")
        }
    }
}

kotlin { jvmToolchain(21) }

tasks {
    patchPluginXml {
        sinceBuild.set("242")
        // Keep the release line open for JetBrains IDE 2024.2+.
        // The IntelliJ Platform Gradle Plugin otherwise derives `until-build="242.*"`
        // from the 2024.2 build platform, which blocks installation on 2025/2026 IDEs.
        untilBuild.set(provider { null })
    }
    val patchedPluginXmlOutput = patchPluginXml.flatMap { it.outputFile }
    val verifyPatchedPluginXmlCompatibility by registering {
        dependsOn(patchPluginXml)
        inputs.file(patchedPluginXmlOutput)
        doLast {
            val pluginXml = patchedPluginXmlOutput.get().asFile.readText()
            require("since-build=\"242\"" in pluginXml) {
                "Patched plugin.xml must keep since-build=\"242\" for JetBrains IDE 2024.2+."
            }
            require("until-build=" !in pluginXml) {
                "Patched plugin.xml must not declare an upper IDE build bound; found an until-build attribute."
            }
        }
    }
    named("check") { dependsOn(verifyPatchedPluginXmlCompatibility) }
    named("buildPlugin") { dependsOn(verifyPatchedPluginXmlCompatibility) }
    withType<JavaExec>().configureEach {
        if (name == "runIde" || name == "buildSearchableOptions") {
            systemProperty("gradle.compatibility.config.url", "")
            systemProperty("gradle.compatibility.update.interval", "0")
        }
    }
    test { useJUnitPlatform() }
}
