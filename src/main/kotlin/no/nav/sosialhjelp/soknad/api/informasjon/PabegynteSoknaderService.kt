package no.nav.sosialhjelp.soknad.api.informasjon

import no.nav.sosialhjelp.soknad.ControllerToNewDatamodellProxy
import no.nav.sosialhjelp.soknad.api.informasjon.dto.PabegyntSoknad
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataService
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadType
import org.springframework.stereotype.Component

@Component
class PabegynteSoknaderService(
    private val soknadMetadataService: SoknadMetadataService,
    private val soknadMetadataRepository: SoknadMetadataRepository,
) {
    fun hentPabegynteSoknaderForBruker(fnr: String): List<PabegyntSoknad> =

        if (ControllerToNewDatamodellProxy.nyDatamodellAktiv) {
            soknadMetadataService.getOpenSoknader(fnr)
                .map {
                    PabegyntSoknad(
                        behandlingsId = it.soknadId.toString(),
                        sistOppdatert = it.tidspunkt.sistEndret,
                        isKort = it.soknadType == SoknadType.KORT,
                    )
                }
        } else {
            soknadMetadataRepository
                .hentPabegynteSoknaderForBruker(fnr)
                .map {
                    PabegyntSoknad(
                        it.sistEndretDato,
                        it.behandlingsId,
                        it.kortSoknad,
                    )
                }
        }
}
