package no.nav.sosialhjelp.soknad

import no.nav.sosialhjelp.soknad.app.MiljoUtils
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder

@SpringBootApplication
class Application {

    // TODO bortkasta todo
    companion object {
        @JvmStatic
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
    }
}
