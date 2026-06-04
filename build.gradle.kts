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
        instrumentationTools()
    }
    testImplementation(kotlin("test"))
}

kotlin { jvmToolchain(17) }

tasks {
    patchPluginXml {
        sinceBuild.set("242")
    }
    test { useJUnitPlatform() }
}

