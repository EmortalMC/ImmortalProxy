import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.10"
    kotlin("kapt") version "1.7.10"
    kotlin("plugin.serialization") version "1.7.10"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "dev.emortal"
version = "1.0.0"

repositories {
    mavenCentral()
    maven(url = "https://nexus.velocitypowered.com/repository/maven-public/")
    maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/")
    maven(url = "https://jitpack.io")
}

dependencies {
    compileOnly("com.velocitypowered:velocity-api:3.0.0")
    kapt("com.velocitypowered:velocity-api:3.0.0")
    //implementation(kotlin("reflect"))
    implementation(kotlin("stdlib-jdk8"))
    implementation("net.kyori:adventure-text-minimessage:4.11.0")

    compileOnly("org.litote.kmongo:kmongo-coroutine-serialization:4.7.1")
//    compileOnly("org.redisson:redisson:3.17.6")
    implementation("redis.clients:jedis:4.3.0")


    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")

    compileOnly("net.luckperms:api:5.4")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}