package no.nav.sosialhjelp.soknad.api.dialog

import no.nav.sosialhjelp.soknad.api.dialog.dto.SistInnsendteSoknadDto
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRepository
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class SistInnsendteSoknadService(
    private val soknadMetadataRepository: SoknadMetadataRepository
) {
    fun hentSistInnsendteSoknad(fnr: String): SistInnsendteSoknadDto? {
        val sistInnsendteSoknad = soknadMetadataRepository.hentAlleInnsendteSoknaderForBruker(fnr)
            .maxByOrNull { it.innsendtDato ?: LocalDateTime.MIN }
        val navEnhet = sistInnsendteSoknad?.navEnhet ?: return null
        val innsendtDato = sistInnsendteSoknad.innsendtDato ?: return null
        return SistInnsendteSoknadDto(
            sistInnsendteSoknad.fnr,
            navEnhet,
            innsendtDato.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        )
    }
}
