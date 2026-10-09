package no.nav.sosialhjelp.soknad.v2.register.fetchers

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.v2.bostotte.BostotteService
import no.nav.sosialhjelp.soknad.v2.register.AsynchronousFetcher
import no.nav.sosialhjelp.soknad.v2.soknad.IntegrasjonStatusService
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class BostotteFetcher(
    private val husbankenService: HusbankenService,
    private val bostotteService: BostotteService,
    private val integrasjonStatusService: IntegrasjonStatusService,
) : AsynchronousFetcher {
    private val logger by logger()

    override suspend fun fetchAndSave(soknadId: UUID) {
        val (saker, inntekt) = try {
            husbankenService.getBostotte()
        } catch (e: Exception) {
            logger.error("Henting av bostøtte fra Husbanken feilet", e)
            // gir bruker mulighet til å legge ved denne informasjonen selv
            bostotteService.addForventetDokumentasjon(soknadId) //todo er det riktig?
            integrasjonStatusService.setStotteHusbankenStatus(soknadId, true)
            return
        }

        bostotteService.saveDataFromHusbanken(soknadId, saker, inntekt)
        integrasjonStatusService.setStotteHusbankenStatus(soknadId, feilet = false)
    }
}
