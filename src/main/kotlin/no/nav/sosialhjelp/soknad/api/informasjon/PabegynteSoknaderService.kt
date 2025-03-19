package no.nav.sosialhjelp.soknad.api.informasjon

import no.nav.sosialhjelp.soknad.api.informasjon.dto.PabegyntSoknad
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataService
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadType
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadService
import org.springframework.stereotype.Component

@Component
class PabegynteSoknaderService(
    private val soknadMetadataService: SoknadMetadataService,
    private val soknadService: SoknadService,
) {
    fun hentPabegynteSoknaderForBruker(fnr: String): List<PabegyntSoknad> =
        soknadService.findOpenSoknadIds(fnr)
            .let { ids -> soknadMetadataService.getMetadatasForIds(ids) }
            .map {
                PabegyntSoknad(
                    behandlingsId = it.soknadId.toString(),
                    sistOppdatert = it.tidspunkt.sistEndret,
                    isKort = it.soknadType == SoknadType.KORT,
                )
            }
}
