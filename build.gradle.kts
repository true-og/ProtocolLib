import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.tasks.Jar
import org.gradle.language.jvm.tasks.ProcessResources

plugins {
    id("java-library")
    id("eclipse")
    id("com.gradleup.shadow") version "8.3.9"
}

group = "com.comphenix.protocol"
version = "5.0.0-dev"
description = "Provides access to the Minecraft protocol"

val isSnapshot = version.toString().endsWith("-SNAPSHOT")

repositories {
    maven {
        url = uri("file://${System.getProperty("user.home")}/.m2/repository")
        content {
            includeGroup("org.spigotmc")
        }
    }
    System.getProperty("SELF_MAVEN_LOCAL_REPO")?.let { // TrueOG Bootstrap mavenLocal().
        val dir = file(it)
        if (dir.isDirectory) {
            println("Using SELF_MAVEN_LOCAL_REPO at: $it")
            maven {
                url = uri("file://${dir.absolutePath}")
                content {
                    includeGroup("org.spigotmc")
                }
            }
        } else {
            logger.error("TrueOG Bootstrap not found, defaulting to ~/.m2 for mavenLocal()")
            mavenLocal {
                content {
                    includeGroup("org.spigotmc")
                }
            }
        }
    } ?: logger.error("TrueOG Bootstrap not found, defaulting to ~/.m2 for mavenLocal()")
    mavenCentral()
    maven {
        name = "Rosewood Public"
        url = uri("https://repo.rosewooddev.io/repository/public/")
        content {
            includeGroup("org.spigotmc")
        }
    }
    maven {
        name = "ElMakers Mirror"
        url = uri("https://maven.elmakers.com/repository/")
        content {
            includeGroup("org.spigotmc")
        }
    }
    maven {
        name = "dmulloy2"
        url = uri("https://repo.dmulloy2.net/repository/public/")
        content {
            excludeGroup("org.spigotmc")
        }
    }
    maven {
        name = "Spigot Snapshots"
        url = uri("https://hub.spigotmc.org/nexus/content/groups/public/")
        content {
            includeGroup("org.spigotmc")
        }
    }
    maven {
        url = uri("https://libraries.minecraft.net/")
        metadataSources {
            mavenPom()
            artifact()
            ignoreGradleMetadataRedirection()
        }
    }
}

dependencies {
    implementation("net.bytebuddy:byte-buddy:1.14.3")
    compileOnly("org.spigotmc:spigot-api:1.19.4-R0.1-SNAPSHOT") {
        isTransitive = false
    }
    compileOnly("com.google.guava:guava:31.1-jre")
    compileOnly("org.apache.commons:commons-lang3:3.17.0")
    compileOnly("commons-lang:commons-lang:2.6")
    compileOnly("com.google.code.findbugs:jsr305:3.0.2")
    compileOnly("com.mojang:datafixerupper:6.0.6")
    compileOnly("net.md-5:bungeecord-chat:1.16-R0.4")
    compileOnly("org.yaml:snakeyaml:1.33")
    compileOnly("org.joml:joml:1.10.5")
    compileOnly("io.netty:netty-all:4.0.23.Final")
    compileOnly("net.kyori:adventure-text-serializer-gson:4.13.0")
    compileOnly("com.googlecode.json-simple:json-simple:1.1.1") {
        isTransitive = false
    }
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
    testImplementation("org.mockito:mockito-core:4.11.0")
    testImplementation("org.mockito:mockito-inline:4.11.0")
    testImplementation("io.netty:netty-common:4.1.77.Final")
    testImplementation("io.netty:netty-transport:4.1.77.Final")
    testImplementation("org.spigotmc:spigot-api:1.19.4-R0.1-SNAPSHOT")
    testImplementation("org.spigotmc:spigot:1.19.4-R0.1-SNAPSHOT")
    testImplementation("com.google.guava:guava:31.1-jre")
    testImplementation("org.apache.commons:commons-lang3:3.17.0")
    testImplementation("commons-lang:commons-lang:2.6")
    testImplementation("com.google.code.findbugs:jsr305:3.0.2")
    testImplementation("com.mojang:authlib:3.17.30")
    testImplementation("com.mojang:brigadier:1.0.18")
    testImplementation("com.mojang:datafixerupper:6.0.6")
    testImplementation("com.mojang:logging:1.1.1")
    testImplementation("net.md-5:bungeecord-chat:1.16-R0.4")
    testImplementation("org.apache.logging.log4j:log4j-api:2.17.1")
    testRuntimeOnly("org.slf4j:slf4j-api:2.0.0")
    testImplementation("org.yaml:snakeyaml:1.33")
    testImplementation("org.joml:joml:1.10.5")
    testImplementation("net.kyori:adventure-text-serializer-gson:4.13.0")
    testImplementation("net.kyori:adventure-text-serializer-plain:4.13.1")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.named<ShadowJar>("shadowJar") {
    dependencies {
        include(dependency("net.bytebuddy:byte-buddy:.*"))
    }
    relocate("net.bytebuddy", "com.comphenix.net.bytebuddy")
    archiveFileName.set("ProtocolLib.jar")
}

tasks.named<Jar>("jar") {
    enabled = false
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
