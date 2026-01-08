
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    `jvm-test-suite`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.spring)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.versions)
    alias(libs.plugins.flyway)
    alias(libs.plugins.spotless)
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

spotless {
    format("misc") {
        target("*.md", ".gitignore", "Dockerfile")

        trimTrailingWhitespace()
        leadingTabsToSpaces()
        endWithNewline()
    }
    kotlin { ktlint(libs.versions.ktlint.get()) }
    kotlinGradle { ktlint(libs.versions.ktlint.get()) }
}

val installHook = tasks.getByName<com.diffplug.gradle.spotless.SpotlessInstallPrePushHookTask>("spotlessInstallGitPrePushHook")

tasks.assemble.get().dependsOn(installHook)

flyway {
    encoding = "ISO-8859-1"
}

val githubUser: String? by project
val githubPassword: String? by project

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://plugins.gradle.org/m2/")
    maven("https://repo.spring.io/plugins-release/")
    maven {
        url = uri("https://maven.pkg.github.com/navikt/*")
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

    // Resilience4j (Retry, CircuitBreaker, ...)
    implementation(libs.bundles.resilience4j)

    implementation(libs.jsonassert)

    // Coroutines
    implementation(libs.bundles.coroutines)

    // filformat
    implementation(libs.soknadsosialhjelp.filformat)

    // sosialhjelp-common
    implementation(libs.bundles.sosialhjelp.common)

    // KS / Fiks
//    implementation(libs.svarut.rest.klient)
    implementation(libs.kryptering)

    // springdoc
    implementation(libs.springdoc.openapi.starter.common)
    implementation(libs.springdoc.openapi.starter.webmvc.ui)

    // flyway / db
    implementation(libs.flyway.core)
    runtimeOnly(libs.flyway.database.postgres)
    // denne kan være runtimeOnly - men pga. UpsertRepositorys midlertidige feilhåndtering må den være implementation
    implementation(libs.postgres)

    // redis
    implementation(libs.lettuce.core)

    // token validering
    implementation(libs.token.validation.spring)
    implementation(libs.java.jwt)

    // Micrometer/prometheus
    implementation(libs.simpleclient)
    implementation(libs.micrometer.registry.prometheus)

    // Opentelemetry
    implementation(platform(libs.opentelemetry.bom))
    implementation(libs.opentelemetry.api)
    implementation(libs.opentelemetry.instrumentation.logback)

    // jackson
    implementation(libs.jackson.module.kotlin)
    implementation(libs.jackson.module.datatype)
    implementation(libs.jackson3.module.kotlin)

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
    testImplementation(libs.testcontainers.postgres)
    testImplementation(libs.testcontainers.junit)

    // Test
    testImplementation(libs.bundles.spring.boot.test)

    testImplementation(libs.token.validation.spring.test)
    testImplementation(libs.mockk)
    testImplementation(libs.mockk.jvm)
    testImplementation(libs.ninja.springmockk)
}

group = "no.nav.sosialhjelp"
version = "18.1.0-SNAPSHOT"
description = "sosialhjelp-soknad-api"

tasks.withType<KotlinCompile> {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
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

tasks.withType<Test> {
    jvmArgs("-Xmx2g")
}
