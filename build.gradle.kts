import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.1.10"
    kotlin("plugin.serialization") version "1.8.0"
    id("io.ktor.plugin") version "3.0.3"
    id("com.google.devtools.ksp") version "2.1.10-1.0.29"
    id("eu.vendeli.telegram-bot") version "7.9.0"
    idea
}

group = "me.hawai"
version = "1.0"

repositories {
    mavenCentral()
}

val koin_version: String by project
val logback_version: String by project
val postgres_version: String by project
val exposed_version: String by project

dependencies {
    implementation("io.github.smiley4:ktor-swagger-ui:4.1.6") // Swagger
    implementation("io.github.smiley4:schema-kenerator-reflection:1.6.3")
    implementation("io.github.smiley4:schema-kenerator-core:1.6.3")
    implementation("io.github.smiley4:schema-kenerator-jsonschema:1.6.3")
    implementation("io.github.smiley4:schema-kenerator-swagger:1.6.3")
    implementation("org.postgresql:postgresql:$postgres_version") // БД
    implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-json:$exposed_version")
    implementation("io.ktor:ktor-server-core-jvm") // Ktor - web-фреймворк
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm")
    implementation("io.ktor:ktor-server-content-negotiation-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("io.ktor:ktor-server-status-pages")
    implementation("io.ktor:ktor-server-cors")
    implementation("io.ktor:ktor-server-resources")
    implementation("io.ktor:ktor-server-request-validation")
    implementation("io.ktor:ktor-server-metrics-micrometer")
    implementation("io.ktor:ktor-client-core")
    implementation("io.ktor:ktor-client-cio")
    implementation("io.ktor:ktor-client-content-negotiation")
    implementation("ch.qos.logback:logback-classic:$logback_version") // логирование
    implementation("io.insert-koin:koin-core:$koin_version") // DI
    implementation("io.insert-koin:koin-ktor:$koin_version")
    implementation("io.insert-koin:koin-logger-slf4j:$koin_version")
    implementation("io.konform:konform:0.10.0") // валидация
    implementation("com.sksamuel.aedile:aedile-core:2.0.3") // кэширование
    implementation("com.zaxxer:HikariCP:6.2.1")
    implementation("io.ktor:ktor-client-cio-jvm:3.0.3")
    testImplementation(kotlin("test")) // тестирование
    testImplementation("io.ktor:ktor-server-test-host")
    testImplementation("io.ktor:ktor-client-content-negotiation")
    testImplementation("io.ktor:ktor-client-mock")
    testImplementation("io.kotest:kotest-runner-junit5:5.9.1")
    testImplementation("io.kotest:kotest-assertions-core:5.9.1")
    testImplementation("io.kotest:kotest-property:5.9.1")
    testImplementation("io.kotest.extensions:kotest-assertions-ktor:2.0.0")
    testImplementation("io.kotest.extensions:kotest-extensions-testcontainers:2.0.2")
    testImplementation("com.h2database:h2:2.3.232")
    testImplementation("org.testcontainers:postgresql:1.20.0")
    testImplementation("io.insert-koin:koin-test:$koin_version")
    testImplementation("io.mockk:mockk:1.13.16")
}

idea {
    module {
        isDownloadSources = true
    }
}

application {
    mainClass.set("me.hawai.HawaiApplicationKt")
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
        options.release.set(17)
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    compileKotlin {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
    }

    build {
        dependsOn(shadowJar)
    }
}