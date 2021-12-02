package no.nav.sosialhjelp.soknad.api.dialog

import no.nav.sosialhjelp.soknad.api.dialog.dto.SistInnsendteSoknadDto
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadmetadata.SoknadMetadataRepository
import java.time.format.DateTimeFormatter

class SistInnsendteSoknadService(
    private val soknadMetadataRepository: SoknadMetadataRepository
) {
    fun hentSistInnsendteSoknad(fnr: String?): SistInnsendteSoknadDto? {
        return soknadMetadataRepository.hentAlleInnsendteSoknaderForBruker(fnr)
            ?.maxByOrNull { it.innsendtDato }
            ?.let {
                SistInnsendteSoknadDto(
                    it.fnr,
                    it.navEnhet,
                    it.innsendtDato.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                )
            }
    }
}
