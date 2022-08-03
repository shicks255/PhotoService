import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.4.3"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("jacoco")
    id("io.gitlab.arturbosch.detekt") version "1.16.0-RC2"
    id("org.jlleitschuh.gradle.ktlint") version "10.1.0"
    kotlin("jvm") version "1.4.30"
    kotlin("plugin.spring") version "1.4.30"
    kotlin("plugin.jpa") version "1.4.30"
}

group = "com.steven.hicks"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-jersey")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.2")
    implementation("org.postgresql:postgresql")
    implementation("org.apache.commons:commons-csv:1.5")
    implementation("org.imgscalr:imgscalr-lib:4.2")
    implementation("com.drewnoakes:metadata-extractor:2.12.0")

    implementation("org.springdoc:springdoc-openapi-ui:1.5.0")

    implementation("io.micrometer:micrometer-registry-prometheus:latest.release")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
// 	testImplementation("org.junit.jupiter:junit-jupiter-engine:5.3.2")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.test)
    classDirectories.setFrom(
        files(
            classDirectories.files.map {
                fileTree(it) {
                    exclude("com/steven/hicks/photoService/models/**")
                    exclude("com/steven/hicks/photoService/config/**")
                    exclude("com/steven/hicks/photoService/repositories/**")
                }
            }
        )
    )
    violationRules {
        rule {
            limit {
                minimum = BigDecimal("0.50")
            }
        }
    }
}

tasks.check {
    doLast { tasks.jacocoTestCoverageVerification }
}

tasks.register("copy") {
    group = "custom"
    description = "Copy the jar service file to my server"
    doLast {
        println("Hello from Gradle :)")
    }
}

tasks.register<Copy>("copyJar") {
    from("$buildDir/libs/photoService-0.0.1-SNAPSHOT.jar")
    into("\\\\thinkcentre\\caddy\\photoService")
}
