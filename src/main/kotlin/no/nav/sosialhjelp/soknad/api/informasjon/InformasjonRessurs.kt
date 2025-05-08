package no.nav.sosialhjelp.soknad.api.informasjon

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.sosialhjelp.soknad.adressesok.AdressesokService
import no.nav.sosialhjelp.soknad.adressesok.domain.AdresseForslag
import no.nav.sosialhjelp.soknad.api.informasjon.dto.Logg
import no.nav.sosialhjelp.soknad.api.informasjon.dto.LoggLevel
import no.nav.sosialhjelp.soknad.api.informasjon.dto.PabegyntSoknad
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.annotation.ProtectionSelvbetjeningHigh
import no.nav.sosialhjelp.soknad.personalia.person.PersonService
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataService
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadType
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.util.unit.DataSize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken as personId

/**
 * Håndterer informasjon om bruker og nylig innsendte/ufullstendige søknader,
 * samt adressesøk.
 */
@RestController
@ProtectionSelvbetjeningHigh
@RequestMapping("/informasjon")
class InformasjonRessurs(
    private val adresseSokService: AdressesokService,
    private val personService: PersonService,
    private val metadataService: SoknadMetadataService,
    private val soknadService: SoknadService,
    @Value("\${spring.servlet.multipart.max-file-size}") private val maxUploadSize: DataSize,
) {
    @GetMapping("/adressesok")
    fun adresseSok(
        @RequestParam("sokestreng") sokestreng: String?,
    ): List<AdresseForslag> = adresseSokService.sokEtterAdresser(sokestreng)

    @PostMapping("/actions/logg")
    fun loggFraKlient(
        @RequestBody logg: Logg,
    ) = when (logg.level) {
        LoggLevel.INFO -> klientlogger.info(logg.melding())
        LoggLevel.WARN -> klientlogger.warn(logg.melding())
        LoggLevel.ERROR -> klientlogger.error(logg.melding())
    }

    @GetMapping("/session")
    fun getSessionInfo(): SessionResponse {
        val eier = personId()
        log.debug("Henter søknadsinfo for bruker")

        return personService.harAdressebeskyttelse(eier)
            .also { if (it) handleHasAdressebeskyttelse() }
            .let {
                SessionResponse(
                    userBlocked = it,
                    daysBeforeDeletion = FJORTEN_DAGER,
                    open = hentPabegynteSoknader(it),
                    numRecentlySent = getNumRecentlySent(it),
                    maxUploadSizeBytes = maxUploadSize.toBytes(),
                )
            }
    }

    private fun handleHasAdressebeskyttelse() {
        soknadService.findOpenSoknadIds(personId()).also { metadataService.deleteAll(it) }
    }

    private fun hentPabegynteSoknader(isGradert: Boolean): List<PabegyntSoknad> =
        when (isGradert) {
            true -> emptyList()
            else ->
                soknadService
                    .findOpenSoknadIds(personId())
                    .let { metadataService.getMetadatasForIds(it) }
                    .map { it.toPabegyntSoknad() }
        }

    private fun getNumRecentlySent(isGradert: Boolean): Int =
        when (isGradert) {
            true -> 0
            else -> {
                metadataService.getNumberOfSoknaderSentAfter(
                    personId = personId(),
                    minusDays = LocalDateTime.now().minusDays(FJORTEN_DAGER),
                )
            }
        }

    companion object {
        private val log by logger()
        private val klientlogger = LoggerFactory.getLogger("klientlogger")
        private const val FJORTEN_DAGER: Long = 14
    }
}

@Schema(description = "Informasjon om brukerøkt")
data class SessionResponse(
    @Schema(description = "Bruker har adressebeskyttelse og kan ikke bruke digital søknad")
    val userBlocked: Boolean,
    @Schema(description = "Antall dager før søknader slettes")
    val daysBeforeDeletion: Long,
    @Schema(description = "Påbegynte men ikke innleverte søknader")
    val open: List<PabegyntSoknad>,
    @Schema(description = "Antall nylig innsendte søknader")
    val numRecentlySent: Int,
    @Schema(description = "Max file upload size, in bytes")
    val maxUploadSizeBytes: Long,
)

private fun SoknadMetadata.toPabegyntSoknad() =
    PabegyntSoknad(
        behandlingsId = soknadId.toString(),
        sistOppdatert = tidspunkt.sistEndret,
        isKort = soknadType == SoknadType.KORT,
    )
