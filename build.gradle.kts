import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

object Versions {
    const val springBoot = "3.2.0" // Husk å oppdatere plugin også
    const val coroutines = "1.6.4"
    const val filformat = "1.2023.06.21-14.54-583dfcc41d77"
    const val sosialhjelpCommon = "1.20231127.1050-86ba0df"
    const val fiksSvarUt = "1.2.0"
    const val fiksKryptering = "1.3.1"
    const val springdoc = "2.1.0"
    const val flyway = "9.16.1" // Husk å oppdatere plugin også
    const val ojdbc10 = "19.18.0.0"
    const val hsqldb = "2.7.1"
    const val lettuce = "6.2.3.RELEASE"
    const val tokenValidation = "3.2.0"
    const val javaJwt = "4.3.0"
    const val prometheus = "0.16.0"
    const val micrometer = "1.10.5"
    const val jackson = "2.14.2"
    const val logback = "1.4.5"
    const val logstash = "7.3"
    const val pdfbox = "3.0.0"
    const val emojiJava = "5.1.1"
    const val jakartaServlet = "6.0.0"
    const val unleashClient = "8.4.0"
    const val tika = "2.7.0"
    const val commonsText = "1.10.0"
    const val ktlint = "0.45.2"
    const val mockk = "1.13.4"
}

plugins {
    kotlin("jvm") version "1.9.20"
    kotlin("plugin.spring") version "1.9.20"
    id("org.springframework.boot") version "3.2.0"
    id("org.flywaydb.flyway") version "10.0.1"
    id("com.github.ben-manes.versions") version "0.50.0" // ./gradlew dependencyUpdates
    id("org.jlleitschuh.gradle.ktlint") version "11.6.1"
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

ktlint {
    this.version.set(Versions.ktlint)
}

flyway {
    encoding = "ISO-8859-1"
}

val githubUser: String by project
val githubPassword: String by project

repositories {
    mavenCentral()
    maven("https://plugins.gradle.org/m2/")
    maven("https://repo.spring.io/plugins-release/")
    maven {
        url = uri("https://maven.pkg.github.com/navikt/sosialhjelp-common")
        credentials {
            username = githubUser
            password = githubPassword
        }
    }
}

configurations {
    testImplementation {
        exclude(group = "org.mockito")
    }
}

dependencies {
    // Spring
    implementation("org.springframework.boot:spring-boot-starter-web:${Versions.springBoot}")
    implementation("org.springframework.boot:spring-boot-starter-webflux:${Versions.springBoot}")
    implementation("org.springframework.boot:spring-boot-starter-validation:${Versions.springBoot}")
    implementation("org.springframework.boot:spring-boot-starter-jdbc:${Versions.springBoot}")
    implementation("org.springframework.boot:spring-boot-starter-actuator:${Versions.springBoot}")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j:${Versions.coroutines}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:${Versions.coroutines}")

    // filformat
    implementation("no.nav.sbl.dialogarena:soknadsosialhjelp-filformat:${Versions.filformat}")

    // sosialhjelp-common
    implementation("no.nav.sosialhjelp:sosialhjelp-common-api:${Versions.sosialhjelpCommon}")
    implementation("no.nav.sosialhjelp:sosialhjelp-common-selftest:${Versions.sosialhjelpCommon}")
    implementation("no.nav.sosialhjelp:sosialhjelp-common-kotlin-utils:${Versions.sosialhjelpCommon}")

    // KS / Fiks
    implementation("no.ks.fiks.svarut:svarut-rest-klient:${Versions.fiksSvarUt}")
    implementation("no.ks.fiks:kryptering:${Versions.fiksKryptering}")

    // springdoc
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${Versions.springdoc}")
    implementation("org.springdoc:springdoc-openapi-starter-common:${Versions.springdoc}")

    // flyway / db
    implementation("org.flywaydb:flyway-core:${Versions.flyway}")
    runtimeOnly("com.oracle.database.jdbc:ojdbc10:${Versions.ojdbc10}")
    runtimeOnly("org.hsqldb:hsqldb:${Versions.hsqldb}")

    // redis
    implementation("io.lettuce:lettuce-core:${Versions.lettuce}")

    // token validering
    implementation("no.nav.security:token-validation-spring:${Versions.tokenValidation}")
    implementation("com.auth0:java-jwt:${Versions.javaJwt}")

    // Micrometer/prometheus
    implementation("io.prometheus:simpleclient:${Versions.prometheus}")
    implementation("io.micrometer:micrometer-registry-prometheus:${Versions.micrometer}")

    // jackson
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${Versions.jackson}")

    // Logging
    implementation("ch.qos.logback:logback-classic:${Versions.logback}")
    implementation("net.logstash.logback:logstash-logback-encoder:${Versions.logstash}")

    // jakarta
    implementation("jakarta.servlet:jakarta.servlet-api:${ Versions.jakartaServlet }")

    // Unleash
    implementation("io.getunleash:unleash-client-java:${Versions.unleashClient}")

    // Tika
    implementation("org.apache.tika:tika-core:${Versions.tika}")
    implementation("org.apache.tika:tika-parsers-standard-package:2.9.1")

    // commons
    implementation("org.apache.commons:commons-text:${Versions.commonsText}")

    // pdf
    implementation("org.apache.pdfbox:pdfbox:${Versions.pdfbox}")
    implementation("org.apache.pdfbox:preflight:${Versions.pdfbox}")
    implementation("org.apache.pdfbox:pdfbox-io:${Versions.pdfbox}")
    implementation("com.vdurmont:emoji-java:${Versions.emojiJava}")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test:${Versions.springBoot}")
    testImplementation("no.nav.security:token-validation-spring-test:${Versions.tokenValidation}")
    // i versjon < 2.0.1 brukes en metode som fører til feil på windows
    // denne kan fjernes når token-validation-spring-test er oppdatert med denne versjonen
    testImplementation("no.nav.security:mock-oauth2-server") {
        version { strictly("2.0.1") }
    }
    testImplementation("io.mockk:mockk:${Versions.mockk}")
    testImplementation("io.mockk:mockk-jvm:${Versions.mockk}")
}

group = "no.nav.sosialhjelp"
version = "18.1.0-SNAPSHOT"
description = "sosialhjelp-soknad-api"

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events = setOf(TestLogEvent.SKIPPED, TestLogEvent.FAILED)
        exceptionFormat = TestExceptionFormat.FULL
        showCauses = true
        showExceptions = true
        showStackTraces = true
    }
}

tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    this.archiveFileName.set("app.jar")
}

fun String.isNonStable(): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { toUpperCase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(this)
    return isStable.not()
}

tasks.withType<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask> {
    rejectVersionIf {
        candidate.version.isNonStable() && !currentVersion.isNonStable()
    }
}
