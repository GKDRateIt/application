import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    kotlin("jvm") version "1.7.10"
    kotlin("plugin.serialization") version "1.6.21"
}

group = "com.github.gkdrateit"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
}

val exposedVersion: String by project

dependencies {
    // kotlin coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.2")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.7.10")
    // slg4j
    implementation("org.slf4j:slf4j-simple:1.7.36")
    // https://mvnrepository.com/artifact/io.javalin/javalin
    implementation("io.javalin:javalin:4.6.1")
//    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.3")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.13.3")
    // kotlin orm
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")
    // pgjdbc-ng driver
    implementation("org.postgresql:postgresql:42.3.6")

    testImplementation(kotlin("test"))
    testImplementation("io.javalin:javalin-testtools:4.6.1")
//    testImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
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