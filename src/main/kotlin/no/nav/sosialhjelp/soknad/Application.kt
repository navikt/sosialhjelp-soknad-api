package no.nav.sosialhjelp.soknad

import no.nav.sosialhjelp.soknad.common.MiljoUtils
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder

@SpringBootApplication
open class Application {

    fun configure(builder: SpringApplicationBuilder): SpringApplicationBuilder {
        return configureApplication(builder)
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            configureApplication(SpringApplicationBuilder())
                .run(*args)
                .registerShutdownHook()
        }

        private fun configureApplication(builder: SpringApplicationBuilder): SpringApplicationBuilder {
            if (!MiljoUtils.isNonProduction() && MiljoUtils.isTillatMock()) {
                throw Error("Env variabel TILLATMOCK har blitt satt til true i prod. Stopper applikasjonen da dette er en sikkerhetsrisiko.")
            }
            if (!MiljoUtils.isNonProduction() && MiljoUtils.isRunningWithInMemoryDb()) {
                throw Error("Env variabel IN_MEMORY_DATABASE har blitt satt til true i prod. Stopper applikasjonen da dette er en sikkerhetsrisiko.")
            }
            if (!MiljoUtils.isNonProduction() && (MiljoUtils.isAlltidHentKommuneInfoFraNavTestkommune() || MiljoUtils.isAlltidSendTilNavTestkommune())) {
                throw Error("Alltid send eller hent fra NavTestkommune er satt til true i prod. Stopper applikasjonen da dette er en sikkerhetsrisiko.")
            }
            return builder
                .sources(Application::class.java)
        }
    }
}
