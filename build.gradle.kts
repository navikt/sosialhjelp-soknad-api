import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    `jvm-test-suite`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.spring)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.versions)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.flyway)
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

ktlint {
    this.version.set(libs.versions.ktlint)
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
    implementation(libs.bundles.spring.boot)

    developmentOnly(libs.spring.boot.docker.compose)

    implementation("org.skyscreamer:jsonassert:1.5.1")

    // Coroutines
    implementation(libs.bundles.coroutines)

    // filformat
    implementation(libs.soknadsosialhjelp.filformat)

    // sosialhjelp-common
    implementation(libs.bundles.sosialhjelp.common)

    // KS / Fiks
    implementation(libs.svarut.rest.klient)
    implementation(libs.kryptering)

    // springdoc
    implementation(libs.springdoc.openapi.starter.common)
    implementation(libs.springdoc.openapi.starter.webmvc.ui)

    // flyway / db
    implementation(libs.flyway.core)
    implementation(libs.vault.jdbc)
    runtimeOnly("org.flywaydb:flyway-database-postgresql:10.11.0")
    runtimeOnly("org.postgresql:postgresql:42.7.3")

    // redis
    implementation(libs.lettuce.core)

    // token validering
    implementation(libs.token.validation.spring)
    implementation(libs.java.jwt)

    // Micrometer/prometheus
    implementation(libs.simpleclient)
    implementation(libs.micrometer.registry.prometheus)

    // jackson
    implementation(libs.jackson.module.kotlin)

    // Logging
    implementation(libs.logback.classic)
    implementation(libs.logstash.logback.encoder)

    // jakarta
    implementation(libs.jakarta.servlet.api)

    // Unleash
    implementation(libs.unleash.client.java)

    // Tika
    implementation(libs.tika.core)
    implementation(libs.tika.parsers.standard.`package`)

    // commons
    implementation(libs.commons.text)

    // pdf
    implementation(libs.bundles.pdfbox)

    // testcontainers
    testImplementation("org.testcontainers:postgresql:1.19.7")
    testImplementation("org.testcontainers:junit-jupiter:1.19.7")

    // Test
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.token.validation.spring.test)
    testImplementation(libs.mockk)
    testImplementation(libs.mockk.jvm)
    testImplementation("com.ninja-squad:springmockk:4.0.2")
}

group = "no.nav.sosialhjelp"
version = "18.1.0-SNAPSHOT"
description = "sosialhjelp-soknad-api"

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "21"
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
            targets {
                all {
                    testTask.configure {
                        testLogging {
                            events = setOf(TestLogEvent.SKIPPED, TestLogEvent.FAILED)
                            exceptionFormat = TestExceptionFormat.FULL
                            showCauses = true
                            showExceptions = true
                            showStackTraces = true
                        }
                    }
                }
            }
        }
    }
}

tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    this.archiveFileName.set("app.jar")
}

fun String.isNonStable(): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { uppercase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(this)
    return isStable.not()
}

tasks.withType<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask> {
    rejectVersionIf {
        candidate.version.isNonStable() && !currentVersion.isNonStable()
    }
}
