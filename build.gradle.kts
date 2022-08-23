import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    kotlin("jvm") version "1.7.10"
    kotlin("plugin.serialization") version "1.7.10"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "com.github.gkdrateit"
version = "1.0.0-alpha"

repositories {
    mavenLocal()
    maven(url = "https://maven.aliyun.com/repository/public/")
    mavenCentral()
}

val exposedVersion: String by project

dependencies {
    // kotlin coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.7.10")
    // slg4j
    implementation("org.slf4j:slf4j-simple:1.7.36")
    // javalin
    implementation("io.javalin:javalin:4.6.4")
    // jackson
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.3")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.13.3")
    // guava
    implementation("com.google.guava:guava:31.1-jre")
    // java web token
    implementation("com.auth0:java-jwt:4.0.0")
    // kotlin orm
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")
    // pgjdbc-ng driver
    implementation("org.postgresql:postgresql:42.3.6")
    // jakarta.mail-api
    implementation("com.sun.mail:javax.mail:1.6.2")

    testImplementation(kotlin("test"))
    testImplementation("io.javalin:javalin-testtools:4.6.4")
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