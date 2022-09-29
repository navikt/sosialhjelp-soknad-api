package no.nav.sosialhjelp.soknad.api.minesaker

import no.nav.sosialhjelp.soknad.api.TimeUtils.toUtc
import no.nav.sosialhjelp.soknad.api.minesaker.dto.InnsendtSoknadDto
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Component
class MineSakerMetadataService(
    private val soknadMetadataRepository: SoknadMetadataRepository
) {
    fun hentInnsendteSoknader(fnr: String): List<InnsendtSoknadDto> {
        val innsendteSoknader = soknadMetadataRepository.hentAlleInnsendteSoknaderForBruker(fnr)
        log.debug("Fant ${innsendteSoknader.size} innsendte soknader")
        return innsendteSoknader.firstOrNull()
            ?.innsendtDato
            ?.let {
                listOf(
                    InnsendtSoknadDto(
                        TEMA_NAVN,
                        TEMA_KODE_KOM,
                        toUtc(it, ZoneId.systemDefault()).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    )
                )
            } ?: emptyList()
    }

    companion object {
        private val log = LoggerFactory.getLogger(MineSakerMetadataRessurs::class.java)
        private const val TEMA_NAVN = "Økonomisk sosialhjelp"
        private const val TEMA_KODE_KOM = "KOM"
    }
}
