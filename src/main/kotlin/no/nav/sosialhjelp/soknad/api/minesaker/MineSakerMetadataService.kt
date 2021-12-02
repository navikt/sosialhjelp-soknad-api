package no.nav.sosialhjelp.soknad.api.minesaker

import no.nav.sosialhjelp.soknad.api.TimeUtils.toUtc
import no.nav.sosialhjelp.soknad.api.minesaker.dto.InnsendtSoknadDto
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadmetadata.SoknadMetadataRepository
import org.slf4j.LoggerFactory
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Optional

class MineSakerMetadataService(
    private val soknadMetadataRepository: SoknadMetadataRepository
) {
    fun hentInnsendteSoknader(fnr: String?): List<InnsendtSoknadDto> {
        val innsendteSoknader = Optional
            .ofNullable(soknadMetadataRepository.hentAlleInnsendteSoknaderForBruker(fnr))
            .orElse(emptyList())
        log.debug("Fant {} innsendte soknader", innsendteSoknader.size)
        return innsendteSoknader.firstOrNull()
            ?.let {
                listOf(
                    InnsendtSoknadDto(
                        TEMA_NAVN,
                        TEMA_KODE_KOM,
                        toUtc(it.innsendtDato, ZoneId.systemDefault()).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
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
