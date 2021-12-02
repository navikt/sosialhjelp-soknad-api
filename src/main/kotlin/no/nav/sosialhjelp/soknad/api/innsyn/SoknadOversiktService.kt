package no.nav.sosialhjelp.soknad.api.innsyn

import no.nav.sosialhjelp.soknad.api.LenkeUtils.lagEttersendelseLenke
import no.nav.sosialhjelp.soknad.api.innsyn.dto.SoknadOversiktDto
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadmetadata.SoknadMetadataRepository
import java.sql.Timestamp

class SoknadOversiktService(
    private val soknadMetadataRepository: SoknadMetadataRepository
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
                url = lagEttersendelseLenke(it.behandlingsId)
            )
        }
    }

    companion object {
        const val KILDE_SOKNAD_API = "soknad-api"
        const val DEFAULT_TITTEL = "Økonomisk sosialhjelp"
    }
}
