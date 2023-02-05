import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.21"
    application
}

group = "ru.shinyparadise"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    val kotlinVersion = "1.7.21"
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")

    val tgbotVersion = "5.0.0"
    implementation("dev.inmo:tgbotapi:$tgbotVersion")

    implementation("ical4j:ical4j:0.9.20")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("MainKt")
}