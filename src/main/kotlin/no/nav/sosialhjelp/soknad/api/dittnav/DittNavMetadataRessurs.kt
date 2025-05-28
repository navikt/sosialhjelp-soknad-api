package no.nav.sosialhjelp.soknad.api.dittnav

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.soknad.api.LenkeUtils.lenkeTilPabegyntSoknad
import no.nav.sosialhjelp.soknad.api.TimeUtils.toUtc
import no.nav.sosialhjelp.soknad.api.dittnav.dto.PabegyntSoknadDto
import no.nav.sosialhjelp.soknad.app.Constants.CLAIM_ACR_LEVEL_3
import no.nav.sosialhjelp.soknad.app.Constants.CLAIM_ACR_LEVEL_4
import no.nav.sosialhjelp.soknad.app.Constants.CLAIM_ACR_LOA_HIGH
import no.nav.sosialhjelp.soknad.app.Constants.CLAIM_ACR_LOA_SUBSTANTIAL
import no.nav.sosialhjelp.soknad.app.Constants.TOKENX
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataService
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@RestController
@ProtectedWithClaims(
    issuer = TOKENX,
    combineWithOr = true,
    claimMap = [CLAIM_ACR_LEVEL_3, CLAIM_ACR_LEVEL_4, CLAIM_ACR_LOA_HIGH, CLAIM_ACR_LOA_SUBSTANTIAL],
)
@RequestMapping("/dittnav/pabegynte/aktive", produces = [MediaType.APPLICATION_JSON_VALUE])
class DittNavMetadataRessurs(
    private val metadataService: SoknadMetadataService,
    private val soknadService: SoknadService,
) {
    @GetMapping
    fun hentPabegynteSoknaderForBruker(): List<PabegyntSoknadDto> {
        val personId = SubjectHandlerUtils.getUserIdFromToken()

        return soknadService.findOpenSoknadIds(personId)
            .let { ids -> metadataService.getMetadatasForIds(ids) }
            .map {
                PabegyntSoknadDto(
                    eventTidspunkt = toUtc(it.tidspunkt.opprettet, ZoneId.systemDefault()).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    eventId = "${it.soknadId}_aktiv",
                    grupperingsId = it.soknadId.toString(),
                    tekst = PABEGYNT_SOKNAD_TITTEL,
                    link = lenkeTilPabegyntSoknad(it.soknadId.toString()),
                    sikkerhetsnivaa = SIKKERHETSNIVAA_3, // hvis ikke vil ikke innloggede nivå 3 brukere se noe på Min side
                    sistOppdatert = toUtc(it.tidspunkt.sistEndret, ZoneId.systemDefault()).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    isAktiv = true,
                )
            }
    }
}

private const val PABEGYNT_SOKNAD_TITTEL = "Påbegynt søknad om økonomisk sosialhjelp"
private const val SIKKERHETSNIVAA_3 = 3
