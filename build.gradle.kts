import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.2.4"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.spring") version "1.9.24"
    jacoco
}

group = "com.github.ralfstuckert"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

extra["springCloudAzureVersion"] = "5.9.1"
extra["springCloudVersion"] = "2023.0.0"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-integration")
    implementation("com.azure.spring:spring-cloud-azure-starter-integration-storage-queue")
    implementation("com.azure.spring:spring-cloud-azure-starter-storage")
    implementation("org.springframework.cloud:spring-cloud-stream")
    implementation("com.azure:azure-data-tables")
    implementation("com.azure:azure-identity")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")

    developmentOnly("org.springframework.boot:spring-boot-devtools")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.springframework.integration:spring-integration-test")
    testImplementation("io.kotest:kotest-assertions-core-jvm:5.8.1")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("com.ninja-squad:springmockk:4.0.2")

    testImplementation("org.springframework.cloud:spring-cloud-starter-bootstrap")
    testImplementation(("com.playtika.testcontainers:embedded-azurite:3.1.5"))
}

dependencyManagement {
    imports {
        mavenBom("com.azure.spring:spring-cloud-azure-dependencies:${property("springCloudAzureVersion")}")
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

val jacocoExclude =
    listOf("**/*Application.kt")

tasks.withType<JacocoReport> {
    reports {
        xml.required.set(true)
        csv.required.set(false)
        html.required.set(false)
    }
    classDirectories.setFrom(
        classDirectories.files.map {
            fileTree(it).matching {
                exclude(jacocoExclude)
            }
        },
    )
}

