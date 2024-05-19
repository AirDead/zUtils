plugins {
    kotlin("jvm") version "1.9.23"
    id("io.papermc.paperweight.userdev") version "1.7.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "me.airdead.zutils"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // testImplementation(kotlin("test"))
    paperweight.paperDevBundle("1.19.2-R0.1-SNAPSHOT")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.23")
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}