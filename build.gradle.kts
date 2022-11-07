import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath("org.jetbrains.kotlinx:atomicfu-gradle-plugin:0.18.5")
    }
}

apply(plugin = "kotlinx-atomicfu")

plugins {
    java
    kotlin("jvm") version "1.7.20"
    kotlin("plugin.serialization") version "1.7.20"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("org.sonarqube") version "3.5.0.2730"
}

group = "com.github.gkdrateit"
version = "0.1.0"

repositories {
    mavenLocal()
    maven(url = "https://maven.aliyun.com/repository/public/")
    mavenCentral()
}

sonarqube {
    properties {
        property("sonar.projectKey", "GKDRateIt_back-end")
        property("sonar.organization", "gkdrateit")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}

val exposedVersion: String by project

dependencies {
    // kotlin coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.7.10")
    // slg4j
    implementation("org.slf4j:slf4j-simple:2.0.3")
    // javalin
    implementation("io.javalin:javalin:5.1.3")
    // jackson
    implementation("com.fasterxml.jackson.core:jackson-databind:2.14.0-rc1")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.13.4")
    // guava
    implementation("com.google.guava:guava:31.1-jre")
    // java web token
    implementation("com.auth0:java-jwt:4.2.1")
    // kotlin orm
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")
    // pgjdbc-ng driver
    implementation("org.postgresql:postgresql:42.5.0")
    // jakarta.mail-api
    implementation("com.sun.mail:javax.mail:1.6.2")

    testImplementation(kotlin("test"))
    testImplementation("io.javalin:javalin-testtools:5.1.3")
    testImplementation("com.h2database:h2:2.1.214")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        exceptionFormat = TestExceptionFormat.FULL
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_17.toString()
}

tasks.withType<ShadowJar> {
    archiveFileName.set("rate-it-backend-${rootProject.version}-shadow.jar")
    manifest {
        attributes(mapOf("Main-Class" to "com.github.gkdrateit.MainKt"))
    }
}

tasks.build {
    dependsOn("shadowJar")
}