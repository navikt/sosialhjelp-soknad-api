package no.nav.sosialhjelp.soknad.v2.kontakt

import no.nav.sosialhjelp.soknad.innsending.KortSoknadService
import no.nav.sosialhjelp.soknad.innsending.digisosapi.DigisosApiService
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentlagerService
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class KortSoknadUseCaseHandler(
    private val kortSoknadService: KortSoknadService,
    private val dokumentlagerService: DokumentlagerService,
    private val digisosApiService: DigisosApiService,
) {
    fun resolveKortSoknad(soknadId: UUID) {
        TODO("Not yet implemented")
    }
}
