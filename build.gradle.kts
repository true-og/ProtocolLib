import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.api.tasks.testing.Test
import org.gradle.language.jvm.tasks.ProcessResources

plugins {
    id("java-library")
    id("eclipse")
    id("com.gradleup.shadow") version "8.3.8"
}

group = "com.comphenix.protocol"
version = "5.0.0-dev"
description = "Provides access to the Minecraft protocol"

val isSnapshot = version.toString().endsWith("-SNAPSHOT")

repositories {
    mavenLocal()
    mavenCentral()
    maven {
        url = uri("https://repo.dmulloy2.net/repository/public/")
    }
    maven {
        url = uri("https://hub.spigotmc.org/nexus/content/groups/public/")
    }
    maven {
        url = uri("https://libraries.minecraft.net/")
        metadataSources {
            mavenPom()
            artifact()
            ignoreGradleMetadataRedirection()
        }
    }
    val customMavenLocal = System.getProperty("SELF_MAVEN_LOCAL_REPO")
    if (customMavenLocal != null) {
        val mavenLocalDir = file(customMavenLocal)
        if (mavenLocalDir.isDirectory) {
            println("Using SELF_MAVEN_LOCAL_REPO at: $customMavenLocal")
            maven {
                url = uri("file://${mavenLocalDir.absolutePath}")
            }
        } else {
            logger.error("TrueOG Bootstrap not found, defaulting to ~/.m2 for mavenLocal()")
        }
    } else {
        logger.error("TrueOG Bootstrap not found, defaulting to ~/.m2 to mavenLocal()")
    }
}

dependencies {
    implementation("net.bytebuddy:byte-buddy:1.14.3")
    compileOnly("org.spigotmc:spigot-api:1.19.4-R0.1-SNAPSHOT")
    compileOnly("org.spigotmc:spigot:1.19.4-R0.1-SNAPSHOT")
    compileOnly("io.netty:netty-all:4.0.23.Final")
    compileOnly("net.kyori:adventure-text-serializer-gson:4.13.0")
    compileOnly("com.googlecode.json-simple:json-simple:1.1.1")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
    testImplementation("org.mockito:mockito-core:4.11.0")
    testImplementation("org.mockito:mockito-inline:4.11.0")
    testImplementation("io.netty:netty-common:4.1.77.Final")
    testImplementation("io.netty:netty-transport:4.1.77.Final")
    testImplementation("org.spigotmc:spigot:1.19.4-R0.1-SNAPSHOT")
    testImplementation("net.kyori:adventure-text-serializer-gson:4.13.0")
    testImplementation("net.kyori:adventure-text-serializer-plain:4.13.1")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
    withJavadocJar()
    withSourcesJar()
}

tasks.named<ShadowJar>("shadowJar") {
    dependencies {
        include(dependency("net.bytebuddy:byte-buddy:.*"))
    }
    relocate("net.bytebuddy", "com.comphenix.net.bytebuddy")
    archiveFileName.set("ProtocolLib.jar")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

tasks.named<ProcessResources>("processResources") {
    val includeBuild = isSnapshot && System.getenv("BUILD_NUMBER") != null
    val fullVersion = if (includeBuild) "$version-${System.getenv("BUILD_NUMBER")}" else version
    filesMatching("**/*") {
        expand("version" to fullVersion)
    }
}

tasks.named("build") {
    dependsOn("shadowJar")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
        }
    }
}

