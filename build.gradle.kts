import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.4.10"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("jvm") version "1.5.30"
    kotlin("plugin.spring") version "1.5.30"
    java
}

group = "com.github.rateit"
version = "0.1-PREVIEW"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    maven {
        setUrl("https://maven.aliyun.com/repository/public/")
    }
    maven {
        setUrl("https://maven.aliyun.com/repository/spring/")
    }
    mavenLocal()
    mavenCentral()
}

configurations {
    all {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-log4j2:2.5.4")
    implementation("org.springframework.boot:spring-boot-starter-web:2.5.4")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.4")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.5.30")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.5.30")
    testImplementation("org.springframework.boot:spring-boot-starter-test:2.5.4")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}
