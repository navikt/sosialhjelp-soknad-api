package no.nav.sosialhjelp.soknad.api.informasjon

import no.nav.sosialhjelp.soknad.api.informasjon.dto.PabegyntSoknad
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRepository
import org.springframework.stereotype.Component

@Component
class PabegynteSoknaderService(
    private val soknadMetadataRepository: SoknadMetadataRepository
) {

    fun hentPabegynteSoknaderForBruker(fnr: String): List<PabegyntSoknad> {
        val soknader = soknadMetadataRepository.hentPabegynteSoknaderForBruker(fnr)

        return soknader
            .map {
                PabegyntSoknad(
                    it.sistEndretDato,
                    it.behandlingsId
                )
            }
    }
}
