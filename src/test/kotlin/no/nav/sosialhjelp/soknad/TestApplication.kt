package no.nav.sosialhjelp.soknad

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class TestApplication

fun main(args: Array<String>) {
    runApplication<TestApplication>(*args).registerShutdownHook()
}
