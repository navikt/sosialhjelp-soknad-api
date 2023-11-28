import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

object Versions {
    const val springBoot = "3.1.5" // Husk å oppdatere plugin også
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
    const val tokenValidation = "3.1.8"
    const val javaJwt = "4.3.0"
    const val prometheus = "0.16.0"
    const val micrometer = "1.10.5"
    const val jackson = "2.14.2"
    const val logback = "1.4.5"
    const val logstash = "7.3"
    const val pdfbox = "3.0.0"
    const val jempbox = "1.8.17"
    const val emojiJava = "5.1.1"
    const val jakartaActivation = "2.1.1"
    const val jakartaAnnotation = "2.1.1"
    const val jakartaInject = "2.0.1"
    const val jakartaServlet = "6.0.0"
    const val jakartaXmlBind = "4.0.0"
    const val jakartaValidation = "3.0.2"
    const val unleashClient = "8.4.0"
    const val tika = "2.7.0"
    const val reactorNettyHttp = "1.1.13"
    const val commonsText = "1.10.0"
    const val commonsCodec = "1.15"
    const val jaxbRuntime = "4.0.2"
    const val ktlint = "0.45.2"
    const val junitJupiter = "5.9.2"
    const val mockk = "1.13.4"

    // constraints
    const val slf4j = "2.0.6"
    const val log4j = "2.19.0"
    const val jodatime = "2.12.2"
    const val jsonsmart = "2.4.10"
    const val nimbusOauth2 = "10.7"
    const val json = "20231013"
    const val byteBuddy = "1.12.20"
    const val jbossLogging = "3.5.0.Final"
    const val errorProneAnnotations = "2.15.0"
    const val checkerQual = "3.25.0"
    const val assertj = "3.23.1"
    const val junit = "4.13.2"
    const val mockOauth2Server = "0.5.8"
    const val snakeyaml = "2.0"
    const val springWebMvc = "6.0.9"
    const val nettyHandler = "4.1.101.Final"
    const val bouncyCastle = "1.74"
    const val jettyHttp = "11.0.16"
    const val commonsCompress = "1.24.0"
}

plugins {
    kotlin("jvm") version "1.9.20"
    kotlin("plugin.spring") version "1.9.20"
    id("org.springframework.boot") version "3.1.5"
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
    implementation {
//        exclude(group = "org.springframework.boot", module = "spring-boot-starter-tomcat")
//        exclude(group = "com.google.code.findbugs", module = "jsr305")
    }
    testImplementation {
        // Ved å ekskludere kqueue som Netty-transport tvinger vi en fallback
        // til java NIO, som forhindrer fryser tester på MacOS. Koster noe
        // ytelse, så kan være verdt å besøke igjen senere.
//        exclude(group = "io.netty", module = "netty-transport-native-kqueue")
//        exclude(group = "org.springframework.boot", module = "spring-boot-starter-web")
//        exclude(group = "org.springframework.boot", module = "spring-boot-starter-tomcat")
        exclude(group = "org.hamcrest")
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
        exclude(group = "org.mockito")
        exclude(group = "org.skyscreamer", module = "jsonassert")
    }
}

dependencies {
    // Spring
//    implementation("org.springframework.boot:spring-boot-starter-jetty:${Versions.springBoot}")
    implementation("org.springframework.boot:spring-boot-starter-web:${Versions.springBoot}")
    implementation("org.springframework.boot:spring-boot-starter-webflux:${Versions.springBoot}")
//    implementation("org.springframework.boot:spring-boot-starter-logging:${Versions.springBoot}")
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
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${Versions.jackson}")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${Versions.jackson}")

    // Logging
    implementation("ch.qos.logback:logback-classic:${Versions.logback}")
    implementation("net.logstash.logback:logstash-logback-encoder:${Versions.logstash}")

    // jakarta
    implementation("jakarta.activation:jakarta.activation-api:${Versions.jakartaActivation}")
    implementation("jakarta.annotation:jakarta.annotation-api:${Versions.jakartaAnnotation}")
    implementation("jakarta.inject:jakarta.inject-api:${Versions.jakartaInject}")
    implementation("jakarta.servlet:jakarta.servlet-api") {
        version { strictly(Versions.jakartaServlet) }
    }
    implementation("jakarta.xml.bind:jakarta.xml.bind-api:${Versions.jakartaXmlBind}")
    runtimeOnly("jakarta.validation:jakarta.validation-api:${Versions.jakartaValidation}")

    // Unleash
    implementation("io.getunleash:unleash-client-java:${Versions.unleashClient}")

    // Tika
    implementation("org.apache.tika:tika-core:${Versions.tika}")
    implementation("org.apache.tika:tika-parsers-standard-package:2.9.1")

    // netty
    implementation("io.projectreactor.netty:reactor-netty-http:${Versions.reactorNettyHttp}")

    // commons
    implementation("org.apache.commons:commons-text:${Versions.commonsText}")
    implementation("commons-codec:commons-codec:${Versions.commonsCodec}")

    // jaxb
    runtimeOnly("org.glassfish.jaxb:jaxb-runtime:${Versions.jaxbRuntime}")

    // pdf
    implementation("org.apache.pdfbox:pdfbox:${Versions.pdfbox}")
    implementation("org.apache.pdfbox:preflight:${Versions.pdfbox}")
    implementation("org.apache.pdfbox:pdfbox-io:${Versions.pdfbox}")
    implementation("org.apache.pdfbox:jempbox:${Versions.jempbox}")
    implementation("com.vdurmont:emoji-java:${Versions.emojiJava}")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test:${Versions.springBoot}")
    testImplementation("no.nav.security:token-validation-spring-test:${Versions.tokenValidation}")
    testImplementation("no.nav.security:mock-oauth2-server") {
        version { strictly("2.0.1") }
    }
    testImplementation("org.junit.jupiter:junit-jupiter:${Versions.junitJupiter}")
    testImplementation("io.mockk:mockk:${Versions.mockk}")
    testImplementation("io.mockk:mockk-jvm:${Versions.mockk}")

//    constraints {
//        implementation("org.apache.logging.log4j:log4j-api:${Versions.log4j}") {
//            because("0-day exploit i version 2.0.0-2.14.1")
//        }
//        implementation("org.apache.logging.log4j:log4j-to-slf4j:${Versions.log4j}") {
//            because("0-day exploit i version 2.0.0-2.14.1")
//        }
//        implementation("org.slf4j:slf4j-api") {
//            version { strictly(Versions.slf4j) }
//        }
//        implementation("ch.qos.logback:logback-classic") {
//            version { strictly(Versions.logback) }
//        }
//        implementation("ch.qos.logback:logback-core") {
//            version { strictly(Versions.logback) }
//        }
//        implementation("joda-time:joda-time:${Versions.jodatime}")
//        implementation("net.minidev:json-smart:${Versions.jsonsmart}") {
//            because("https://security.snyk.io/vuln/SNYK-JAVA-NETMINIDEV-3369748")
//        }
//        implementation("com.nimbusds:oauth2-oidc-sdk:${Versions.nimbusOauth2}")
//        implementation("org.json:json:${Versions.json}") {
//            because("https://github.com/advisories/GHSA-3vqj-43w4-2q58")
//        }
//        implementation("net.bytebuddy:byte-buddy:${Versions.byteBuddy}")
//        implementation("net.bytebuddy:byte-buddy-agent:${Versions.byteBuddy}")
//        implementation("org.jboss.logging:jboss-logging:${Versions.jbossLogging}")
//        implementation("com.google.errorprone:error_prone_annotations:${Versions.errorProneAnnotations}")
//        implementation("org.checkerframework:checker-qual:${Versions.checkerQual}")
//
//        implementation("org.yaml:snakeyaml:${Versions.snakeyaml}") {
//            because("https://security.snyk.io/vuln/SNYK-JAVA-ORGYAML-3152153")
//        }
//        implementation("org.springframework:spring-webmvc:${Versions.springWebMvc}") {
//            because("https://github.com/advisories/GHSA-wxqc-pxw9-g2p8")
//        }
//        implementation("io.netty:netty-handler:${Versions.nettyHandler}") {
//            because("https://github.com/advisories/GHSA-6mjq-h674-j845")
//        }
//        implementation("org.bouncycastle:bcprov-jdk18on") {
//            version { strictly(Versions.bouncyCastle) }
//        }
//        implementation("org.eclipse.jetty:jetty-http:${Versions.jettyHttp}") {
//            because("https://github.com/advisories/GHSA-hmr7-m48g-48f6")
//        }
//        implementation("org.apache.commons:commons-compress:${Versions.commonsCompress}") {
//            because("https://github.com/advisories/GHSA-cgwf-w82q-5jrr")
//        }
//
//        testImplementation("org.assertj:assertj-core:${Versions.assertj}")
//        testImplementation("junit:junit:${Versions.junit}")
//        testImplementation("no.nav.security:mock-oauth2-server:${Versions.mockOauth2Server}")
//    }
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
