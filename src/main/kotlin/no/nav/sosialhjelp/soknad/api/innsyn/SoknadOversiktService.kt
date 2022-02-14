package no.nav.sosialhjelp.soknad.api.innsyn

import no.nav.sosialhjelp.soknad.api.LenkeUtils.lagEttersendelseLenke
import no.nav.sosialhjelp.soknad.api.innsyn.dto.SoknadOversiktDto
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.common.ServiceUtils
import java.sql.Timestamp

class SoknadOversiktService(
    private val soknadMetadataRepository: SoknadMetadataRepository,
    private val serviceUtils: ServiceUtils
) {
    fun hentSvarUtSoknaderFor(fnr: String): List<SoknadOversiktDto> {
        val soknader = soknadMetadataRepository.hentSvarUtInnsendteSoknaderForBruker(fnr)
        return soknader.map {
            SoknadOversiktDto(
                fiksDigisosId = null,
                soknadTittel = "$DEFAULT_TITTEL (${it.behandlingsId})",
                status = it.status.toString(),
                sistOppdatert = Timestamp.valueOf(it.sistEndretDato),
                antallNyeOppgaver = null,
                kilde = KILDE_SOKNAD_API,
                url = lagEttersendelseLenke(it.behandlingsId, serviceUtils.environmentName)
            )
        }
    }

    companion object {
        const val KILDE_SOKNAD_API = "soknad-api"
        const val DEFAULT_TITTEL = "Økonomisk sosialhjelp"
    }
}
