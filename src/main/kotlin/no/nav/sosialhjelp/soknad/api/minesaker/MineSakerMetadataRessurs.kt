package no.nav.sosialhjelp.soknad.api.minesaker

import no.nav.security.token.support.core.api.Unprotected
import no.nav.sosialhjelp.soknad.app.annotation.ProtectionSelvbetjeningHigh
import no.nav.sosialhjelp.soknad.app.annotation.ProtectionTokenXSubstantial
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
@RequestMapping("/minesaker", produces = [MediaType.APPLICATION_JSON_VALUE])
class MineSakerMetadataRessurs(
    private val mineSakerService: MineSakerService,
) {
    /**
     * Henter informasjon om innsendte søknader via SoknadMetadataRepository.
     * På sikt vil vi hente denne informasjonen fra Fiks (endepunkt vil da høre mer hjemme i innsyn-api)
     */
    @ProtectionTokenXSubstantial
    @GetMapping("/innsendte")
    fun hentInnsendteSoknaderForBruker(): List<InnsendtSoknadDto> {
        return mineSakerService.hentInnsendteSoknader().map {
            InnsendtSoknadDto(
                navn = TEMA_NAVN,
                kode = TEMA_KODE_KOM,
                sistEndret = it.tidspunkt.sendtInn?.toString() ?: "",
            )
        }
    }

    @Unprotected
    @GetMapping("/ping")
    fun ping(): String {
        log.debug("Ping for MineSaker")
        return "pong"
    }

    @ProtectionSelvbetjeningHigh
    @GetMapping("/antallSisteDogn")
    fun hentAntallInnsendteSoknader(): AntallInnsendteSoknaderDto {
        return mineSakerService.hentInnsendteSoknaderSisteDogn()
            .let { (antall, innsendingTillattFra) ->
                AntallInnsendteSoknaderDto(
                    antall = antall,
                    innsendingTillattFra = innsendingTillattFra,
                    maxAntall = MineSakerService.MAX_ANTALL_SOKNADER,
                )
            }
    }

    companion object {
        private val log = LoggerFactory.getLogger(MineSakerMetadataRessurs::class.java)
        private const val TEMA_NAVN = "Økonomisk sosialhjelp"
        private const val TEMA_KODE_KOM = "KOM"
    }
}

data class AntallInnsendteSoknaderDto(
    val antall: Int,
    val innsendingTillattFra: LocalDateTime?,
    val maxAntall: Int,
)

data class InnsendtSoknadDto(
    val navn: String,
    val kode: String,
    val sistEndret: String,
)
