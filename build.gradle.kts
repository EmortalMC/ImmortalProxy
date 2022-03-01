import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.10"
    kotlin("kapt") version "1.6.10"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "dev.emortal"
version = "1.0.0"

repositories {
    mavenCentral()
    maven(url = "https://nexus.velocitypowered.com/repository/maven-public/")
}

dependencies {
    compileOnly("com.velocitypowered:velocity-api:3.0.0")
    kapt("com.velocitypowered:velocity-api:3.0.0")
    implementation(kotlin("reflect"))
    implementation(kotlin("stdlib-jdk8"))

    //implementation("redis.clients:jedis:4.1.1")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}