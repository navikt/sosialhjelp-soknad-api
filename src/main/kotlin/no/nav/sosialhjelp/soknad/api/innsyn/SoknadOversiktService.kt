package no.nav.sosialhjelp.soknad.api.innsyn

import no.nav.sosialhjelp.soknad.api.LenkeUtils.lagEttersendelseLenke
import no.nav.sosialhjelp.soknad.api.innsyn.dto.SoknadOversiktDto
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRepository
import org.springframework.stereotype.Component
import java.sql.Timestamp

@Component
class SoknadOversiktService(
    private val soknadMetadataRepository: SoknadMetadataRepository,
) {
    fun hentSvarUtSoknaderFor(fnr: String): List<SoknadOversiktDto> {
        val soknader = soknadMetadataRepository.hentSvarUtInnsendteSoknaderForBruker(fnr)
        return soknader.map {
            SoknadOversiktDto(
                fiksDigisosId = null,
                soknadTittel = "$DEFAULT_TITTEL (${it.behandlingsId})",
                sistOppdatert = Timestamp.valueOf(it.sistEndretDato),
                kilde = KILDE_SOKNAD_API,
                url = lagEttersendelseLenke(it.behandlingsId),
            )
        }
    }

    companion object {
        const val KILDE_SOKNAD_API = "soknad-api"
        const val DEFAULT_TITTEL = "Økonomisk sosialhjelp"
    }
}
