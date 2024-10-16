package no.nav.sosialhjelp.soknad.innsending

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknadsmottaker
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.soknad.api.nedetid.NedetidService
import no.nav.sosialhjelp.soknad.app.Constants
import no.nav.sosialhjelp.soknad.app.exceptions.SendingTilKommuneErMidlertidigUtilgjengeligException
import no.nav.sosialhjelp.soknad.app.exceptions.SendingTilKommuneUtilgjengeligException
import no.nav.sosialhjelp.soknad.app.exceptions.SoknadenHarNedetidException
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.innsending.JsonVedleggUtils.addHendelseTypeAndHendelseReferanse
import no.nav.sosialhjelp.soknad.innsending.digisosapi.DigisosApiService
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneInfoService
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneStatus.FIKS_NEDETID_OG_TOM_CACHE
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneStatus.HAR_KONFIGURASJON_MED_MANGLER
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneStatus.MANGLER_KONFIGURASJON
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneStatus.SKAL_SENDE_SOKNADER_VIA_FDA
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneStatus.SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD
import no.nav.sosialhjelp.soknad.innsending.dto.SendTilUrlFrontend
import no.nav.sosialhjelp.soknad.innsending.dto.SoknadMottakerFrontend
import no.nav.sosialhjelp.soknad.navenhet.NavEnhetService
import no.nav.sosialhjelp.soknad.navenhet.NavEnhetUtils.createNavEnhetsnavn
import no.nav.sosialhjelp.soknad.navenhet.dto.NavEnhetFrontend
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import no.nav.sosialhjelp.soknad.v2.json.generate.TimestampConverter
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.time.LocalDateTime

@RestController
@ProtectedWithClaims(
    issuer = Constants.SELVBETJENING,
    claimMap = [Constants.CLAIM_ACR_LEVEL_4, Constants.CLAIM_ACR_LOA_HIGH],
    combineWithOr = true,
)
@RequestMapping("/soknader/{behandlingsId}/actions", produces = [MediaType.APPLICATION_JSON_VALUE])
class SoknadActions(
    private val kommuneInfoService: KommuneInfoService,
    private val tilgangskontroll: Tilgangskontroll,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val digisosApiService: DigisosApiService,
    private val nedetidService: NedetidService,
    private val navEnhetService: NavEnhetService,
) {
    @PostMapping("/send")
    fun sendSoknad(
        @PathVariable("behandlingsId") behandlingsId: String,
        @RequestHeader(value = HttpHeaders.AUTHORIZATION) token: String?,
    ): SendTilUrlFrontend {
        if (nedetidService.isInnenforNedetid) {
            throw SoknadenHarNedetidException("Soknaden har nedetid fram til ${nedetidService.nedetidSluttAsString}")
        }

        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)
        val eier = SubjectHandlerUtils.getUserIdFromToken()

        val soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)

        // TODO Manipulerer data for å sjekke at utlede kommunenummer funker
        soknadUnderArbeid.jsonInternalSoknad?.soknad?.mottaker?.kommunenummer = null
        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknadUnderArbeid, eier)

        updateVedleggJsonWithHendelseTypeAndHendelseReferanse(eier, soknadUnderArbeid)

        val kommunenummer =
            soknadUnderArbeid.jsonInternalSoknad?.soknad?.mottaker?.kommunenummer
                ?: utledOgLagreKommunenummer(soknadUnderArbeid)

        val kommuneStatus = kommuneInfoService.getKommuneStatus(kommunenummer = kommunenummer, withLogging = true)
        log.info("Kommune: $kommunenummer Status: $kommuneStatus")

        return when (kommuneStatus) {
            FIKS_NEDETID_OG_TOM_CACHE ->
                throw SendingTilKommuneUtilgjengeligException(
                    "Sending til kommune $kommunenummer er ikke tilgjengelig fordi fiks har nedetid og kommuneinfo-cache er tom.",
                )
            MANGLER_KONFIGURASJON, HAR_KONFIGURASJON_MED_MANGLER ->
                throw SendingTilKommuneUtilgjengeligException("Manglende eller feil konfigurasjon. (SvarUt)")
            SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD ->
                throw SendingTilKommuneErMidlertidigUtilgjengeligException(
                    "Sending til kommune $kommunenummer er midlertidig utilgjengelig.",
                )
            SKAL_SENDE_SOKNADER_VIA_FDA -> {
                log.info("Sendes til Fiks-digisos-api (sfa. Fiks-konfigurasjon).")
                val forrigeSoknadSendt = hentForrigeSoknadSendt()
                val digisosId = digisosApiService.sendSoknad(soknadUnderArbeid, token, kommunenummer)

                SendTilUrlFrontend(
                    id = digisosId,
                    sendtTil = SoknadMottakerFrontend.FIKS_DIGISOS_API,
                    antallDokumenter = getAntallDokumenter(soknadUnderArbeid.jsonInternalSoknad),
                    forrigeSoknadSendt = forrigeSoknadSendt,
                )
            }
        }
    }

    private fun utledOgLagreKommunenummer(soknadUnderArbeid: SoknadUnderArbeid): String {
        val jsonSoknad =
            soknadUnderArbeid.jsonInternalSoknad?.soknad
                ?: throw IllegalStateException("Kan ikke sette kommunenummer - JsonSoknad er null")

        val adresseValg =
            jsonSoknad.data.personalia.oppholdsadresse?.adresseValg
                ?: throw IllegalStateException("Kan ikke sette kommunenummer - Adressevalg er null")

        val navEnhetFrontend =
            navEnhetService.getNavEnhet(
                eier = SubjectHandlerUtils.getUserIdFromToken(),
                soknad = jsonSoknad,
                valg = adresseValg,
            ) ?: throw IllegalStateException("Kan ikke sette kommunenummer - NavEnhet er null")

        setNavEnhetAsMottaker(soknadUnderArbeid, navEnhetFrontend)
        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknadUnderArbeid, SubjectHandlerUtils.getUserIdFromToken())

        return soknadUnderArbeidRepository.hentSoknad(
            soknadUnderArbeid.behandlingsId,
            SubjectHandlerUtils.getUserIdFromToken(),
        )
            .jsonInternalSoknad?.soknad?.mottaker?.kommunenummer
            ?: throw IllegalStateException("Kan ikke sette kommunenummer - Kommunenummer er null")
    }

    private fun updateVedleggJsonWithHendelseTypeAndHendelseReferanse(
        eier: String,
        soknadUnderArbeid: SoknadUnderArbeid,
    ) {
        val jsonVedleggSpesifikasjon = soknadUnderArbeid.jsonInternalSoknad?.vedlegg ?: return

        addHendelseTypeAndHendelseReferanse(jsonVedleggSpesifikasjon = jsonVedleggSpesifikasjon)

        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknadUnderArbeid, eier)
    }

    private fun hentForrigeSoknadSendt(): LocalDateTime? {
        log.info("Henter tidspunkt for eventuelt forrige søknad sendt.")
        runCatching {
            return digisosApiService.getTimestampSistSendtSoknad(SubjectHandlerUtils.getToken())
                ?.let {
                    TimestampConverter.convertInstantToLocalDateTime(Instant.ofEpochMilli(it))
                }
        }
            .onFailure { log.error("Kunne ikke hente tidspunkt for forrige søknad sendt.", it) }

        return null
    }

    private fun getAntallDokumenter(jsonInternalSoknad: JsonInternalSoknad?): Int {
        return jsonInternalSoknad?.vedlegg?.vedlegg?.flatMap { it.filer }?.size ?: 0
    }

    companion object {
        private val log = LoggerFactory.getLogger(SoknadActions::class.java)
    }
}

private fun setNavEnhetAsMottaker(
    soknadUnderArbeid: SoknadUnderArbeid,
    navEnhetFrontend: NavEnhetFrontend,
) {
    soknadUnderArbeid.jsonInternalSoknad?.mottaker =
        no.nav.sbl.soknadsosialhjelp.soknad.internal
            .JsonSoknadsmottaker()
            .withNavEnhetsnavn(createNavEnhetsnavn(navEnhetFrontend.enhetsnavn, navEnhetFrontend.kommunenavn))
            .withOrganisasjonsnummer(navEnhetFrontend.orgnr)

    soknadUnderArbeid.jsonInternalSoknad?.soknad?.mottaker =
        JsonSoknadsmottaker()
            .withNavEnhetsnavn(createNavEnhetsnavn(navEnhetFrontend.enhetsnavn, navEnhetFrontend.kommunenavn))
            .withEnhetsnummer(navEnhetFrontend.enhetsnr)
            .withKommunenummer(navEnhetFrontend.kommuneNr)
}
