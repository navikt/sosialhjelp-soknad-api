package no.nav.sosialhjelp.soknad.api.informasjon

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.sosialhjelp.soknad.adressesok.AdressesokService
import no.nav.sosialhjelp.soknad.adressesok.domain.AdresseForslag
import no.nav.sosialhjelp.soknad.api.informasjon.dto.Logg
import no.nav.sosialhjelp.soknad.api.informasjon.dto.LoggLevel
import no.nav.sosialhjelp.soknad.api.informasjon.dto.PabegyntSoknad
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.annotation.ProtectionSelvbetjeningHigh
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.innsending.KortSoknadService
import no.nav.sosialhjelp.soknad.personalia.person.PersonService
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataService
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
    private val kortSoknadService: KortSoknadService,
    private val soknadMetadataService: SoknadMetadataService,
    private val pabegynteSoknaderService: PabegynteSoknaderService,
    @Value("\${spring.servlet.multipart.max-file-size}") private val maxUploadSize: DataSize,
) {
    companion object {
        private val log by logger()
        private val klientlogger = LoggerFactory.getLogger("klientlogger")
        private const val FJORTEN_DAGER: Long = 14
    }

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

        val token = SubjectHandlerUtils.getTokenOrNull()
        val userBlocked = personService.harAdressebeskyttelse(eier)
        val person = if (userBlocked) null else personService.hentPerson(eier, hentEktefelle = false)
        val kommunenummer = person?.oppholdsadresse?.vegadresse?.kommunenummer
        val qualifiesForKortSoknad =
            if (kommunenummer != null && token != null) {
                runCatching { kortSoknadService.isQualifiedFromFiks(token, kommunenummer) }
                    .onFailure { log.warn("Fikk feilmelding fra fiks i informasjon/session", it) }
                    .getOrNull()
            } else {
                null
            }
        // Egentlig bør vel hentPerson kaste en exception dersom en bruker ikke finnes
        // for en gitt ID. I første omgang nøyer vi oss med å logge en feilmelding
        // men hentPerson bør nok bli noe mer deterministisk.
        if (person === null) log.error("Fant ikke person for bruker")

        val numRecentlySent =
            soknadMetadataService.getNumberOfSoknaderSentAfter(
                personId = eier,
                minusDays = LocalDateTime.now().minusDays(FJORTEN_DAGER),
            )

        return SessionResponse(
            userBlocked = personService.harAdressebeskyttelse(eier),
            fornavn = person?.fornavn,
            daysBeforeDeletion = FJORTEN_DAGER,
            open = pabegynteSoknaderService.hentPabegynteSoknaderForBruker(eier),
            numRecentlySent = numRecentlySent,
            maxUploadSizeBytes = maxUploadSize.toBytes(),
            qualifiesForKortSoknad = qualifiesForKortSoknad,
        )
    }

    @Schema(description = "Informasjon om brukerøkt")
    data class SessionResponse(
        @Schema(description = "Bruker har adressebeskyttelse og kan ikke bruke digital søknad")
        val userBlocked: Boolean,
        @Schema(description = "Brukerens fornavn")
        val fornavn: String?,
        @Schema(description = "Antall dager etter siste endring før søknader slettes")
        val daysBeforeDeletion: Long,
        @Schema(description = "Påbegynte men ikke innleverte søknader")
        val open: List<PabegyntSoknad>,
        @Schema(description = "Antall nylig innsendte søknader")
        val numRecentlySent: Int,
        @Schema(description = "Max file upload size, in bytes")
        val maxUploadSizeBytes: Long,
        @Schema(description = "User qualifies for kort søknad")
        val qualifiesForKortSoknad: Boolean?,
    )
}
