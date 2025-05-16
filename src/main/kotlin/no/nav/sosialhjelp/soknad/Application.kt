package no.nav.sosialhjelp.soknad

import no.nav.sosialhjelp.soknad.app.MiljoUtils
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

@SpringBootApplication
class Application

fun main(args: Array<String>) {
    configureApplication(SpringApplicationBuilder())
        .run(*args)
        .registerShutdownHook()
}

private fun configureApplication(builder: SpringApplicationBuilder): SpringApplicationBuilder {
    if (!MiljoUtils.isNonProduction() && MiljoUtils.isMockAltProfil()) {
        throw Error("Spring profile `mock-alt` har blitt satt i prod. Stopper applikasjonen da dette er en sikkerhetsrisiko.")
    }
    return builder
        .sources(Application::class.java)
}

fun nowWithMillis(): LocalDateTime =
    ZonedDateTime.now(ZoneId.of("Europe/Oslo")).toLocalDateTime().truncatedTo(ChronoUnit.MILLIS)
