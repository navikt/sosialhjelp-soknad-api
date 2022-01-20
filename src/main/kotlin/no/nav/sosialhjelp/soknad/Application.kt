package no.nav.sosialhjelp.soknad

import no.nav.sosialhjelp.soknad.domain.model.mock.MockUtils
import no.nav.sosialhjelp.soknad.domain.model.util.ServiceUtils
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class Application

fun main(args: Array<String>) {
    if (!ServiceUtils.isNonProduction() && MockUtils.isMockAltProfil()) {
        throw Error("mockAltProfil har blitt satt til true i prod. Stopper applikasjonen da dette er en sikkerhetsrisiko.")
    }
    if (!ServiceUtils.isNonProduction() && MockUtils.isRunningWithInMemoryDb()) {
        throw Error("no.nav.sosialhjelp.soknad.hsqldb har blitt satt til true i prod. Stopper applikasjonen da dette er en sikkerhetsrisiko.")
    }
    if (!ServiceUtils.isNonProduction() && (MockUtils.isAlltidHentKommuneInfoFraNavTestkommune() || MockUtils.isAlltidSendTilNavTestkommune())) {
        throw Error("Alltid send eller hent fra NavTestkommune er satt til true i prod. Stopper applikasjonen da dette er en sikkerhetsrisiko.")
    }

    runApplication<Application>(*args).registerShutdownHook()
}
