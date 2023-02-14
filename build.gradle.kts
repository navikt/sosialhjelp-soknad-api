import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

object Versions {
    const val springBoot = "2.7.8" // Husk å oppdatere plugin også
    const val coroutines = "1.6.4"
    const val filformat = "1.2022.12.22-12.23-e5a89d40cc3c"
    const val sosialhjelpCommon = "1.20221108.1040-97f1b80"
    const val fiksSvarUt = "1.2.0"
    const val fiksKryptering = "1.3.1"
    const val springdoc = "1.6.14"
    const val flyway = "9.12.0" // Husk å oppdatere plugin også
    const val ojdbc10 = "19.17.0.0"
    const val hsqldb = "2.7.1"
    const val lettuce = "6.2.2.RELEASE"
    const val tokenValidation = "2.1.9"
    const val javaJwt = "4.2.2"
    const val prometheus = "0.16.0"
    const val micrometer = "1.10.3"
    const val jackson = "2.14.1"
    const val logback = "1.2.11"
    const val logstash = "7.2"
    const val log4j = "2.19.0"
    const val pdfbox = "2.0.27"
    const val jempbox = "1.8.17"
    const val emojiJava = "5.1.1"
    const val jakartaActivation = "1.2.2"
    const val jakartaAnnotation = "1.3.5"
    const val jakartaInject = "1.0.5"
    const val jakartaServlet = "4.0.4"
    const val jakartaXmlBind = "2.3.3"
    const val jakartaValidation = "2.0.2"
    const val unleashClient = "3.3.4"
    const val tika = "2.6.0"
    const val reactorNettyHttp = "1.1.2"
    const val commonsText = "1.10.0"
    const val commonsCodec = "1.15"
    const val jaxbJavaTimeAdapter = "1.1.3"
    const val jaxbRuntime = "2.3.7"
    const val slf4j = "1.7.36"
    const val ktlint = "0.45.2"
    const val junitJupiter = "5.9.1"
    const val mockk = "1.13.3"
}

plugins {
    kotlin("jvm") version "1.7.22"
    kotlin("plugin.spring") version "1.7.22"
    id("org.springframework.boot") version "2.7.8"
    id("org.flywaydb.flyway") version "9.12.0"
    id("com.github.ben-manes.versions") version "0.45.0" // ./gradlew dependencyUpdates
    id("org.jlleitschuh.gradle.ktlint") version "11.0.0"
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

ktlint {
    this.version.set(Versions.ktlint)
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
    implementation {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-tomcat")
    }
    testImplementation {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-tomcat")
        exclude(group = "org.hamcrest", module = "hamcrest-library")
        exclude(group = "org.hamcrest", module = "hamcrest-core")
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
        exclude(group = "org.mockito", module = "mockito-core")
    }
}

dependencies {
    // Spring
    implementation("org.springframework.boot:spring-boot-starter-jetty:${Versions.springBoot}")
    implementation("org.springframework.boot:spring-boot-starter-webflux:${Versions.springBoot}")
    implementation("org.springframework.boot:spring-boot-starter-logging:${Versions.springBoot}")
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

    // KS / Fiks
    implementation("no.ks.fiks.svarut:svarut-rest-klient:${Versions.fiksSvarUt}")
    implementation("no.ks.fiks:kryptering:${Versions.fiksKryptering}")

    // springdoc
    implementation("org.springdoc:springdoc-openapi-ui:${Versions.springdoc}")
    implementation("org.springdoc:springdoc-openapi-kotlin:${Versions.springdoc}")

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
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${Versions.jackson}")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${Versions.jackson}")

    // Logging
    implementation("ch.qos.logback:logback-classic:${Versions.logback}")
    implementation("net.logstash.logback:logstash-logback-encoder:${Versions.logstash}")

    // jakarta
    implementation("jakarta.activation:jakarta.activation-api:${Versions.jakartaActivation}")
    implementation("jakarta.annotation:jakarta.annotation-api:${Versions.jakartaAnnotation}")
    implementation("jakarta.inject:jakarta.inject-api:${Versions.jakartaInject}")
    implementation("jakarta.servlet:jakarta.servlet-api:${Versions.jakartaServlet}")
    implementation("jakarta.xml.bind:jakarta.xml.bind-api:${Versions.jakartaXmlBind}")
    runtimeOnly("jakarta.validation:jakarta.validation-api:${Versions.jakartaValidation}")

    // Unleash
    implementation("no.finn.unleash:unleash-client-java:${Versions.unleashClient}")

    // Tika
    implementation("org.apache.tika:tika-core:${Versions.tika}")

    // netty
    implementation("io.projectreactor.netty:reactor-netty-http:${Versions.reactorNettyHttp}")

    // commons
    implementation("org.apache.commons:commons-text:${Versions.commonsText}")
    implementation("commons-codec:commons-codec:${Versions.commonsCodec}")

    // jaxb
    implementation("com.migesok:jaxb-java-time-adapters:${Versions.jaxbJavaTimeAdapter}")
    runtimeOnly("org.glassfish.jaxb:jaxb-runtime:${Versions.jaxbRuntime}")

    // pdf
    implementation("org.apache.pdfbox:pdfbox:${Versions.pdfbox}")
    implementation("org.apache.pdfbox:preflight:${Versions.pdfbox}")
    implementation("org.apache.pdfbox:jempbox:${Versions.jempbox}")
    implementation("com.vdurmont:emoji-java:${Versions.emojiJava}")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test:${Versions.springBoot}")
    testImplementation("no.nav.security:token-validation-spring-test:${Versions.tokenValidation}")
    testImplementation("org.junit.jupiter:junit-jupiter:${Versions.junitJupiter}")
    testImplementation("io.mockk:mockk:${Versions.mockk}")
    testImplementation("io.mockk:mockk-jvm:${Versions.mockk}")

    constraints {
        implementation("org.apache.logging.log4j:log4j-api:${Versions.log4j}") {
            because("0-day exploit i version 2.0.0-2.14.1")
        }
        implementation("org.apache.logging.log4j:log4j-to-slf4j:${Versions.log4j}") {
            because("0-day exploit i version 2.0.0-2.14.1")
        }
        implementation("org.slf4j:slf4j-api") {
            version { strictly(Versions.slf4j) }
        }
    }
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
