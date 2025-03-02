package no.nav.sosialhjelp.soknad.api.informasjon

import no.nav.sosialhjelp.soknad.api.informasjon.dto.PabegyntSoknad
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataService
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadType
import org.springframework.stereotype.Component

@Component
class PabegynteSoknaderService(
    private val soknadMetadataService: SoknadMetadataService,
) {
    fun hentPabegynteSoknaderForBruker(fnr: String): List<PabegyntSoknad> =
        soknadMetadataService.getOpenSoknader(fnr)
            .map {
                PabegyntSoknad(
                    behandlingsId = it.soknadId.toString(),
                    sistOppdatert = it.tidspunkt.sistEndret,
                    isKort = it.soknadType == SoknadType.KORT,
                )
            }
}
