package no.nav.sosialhjelp.soknad.personalia.adresse

import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknadsmottaker
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresseValg
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.soknad.app.Constants
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.navenhet.NavEnhetService
import no.nav.sosialhjelp.soknad.navenhet.NavEnhetUtils
import no.nav.sosialhjelp.soknad.navenhet.dto.NavEnhetFrontend
import no.nav.sosialhjelp.soknad.personalia.adresse.dto.AdresserFrontend
import no.nav.sosialhjelp.soknad.personalia.adresse.dto.AdresserFrontendInput
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@ProtectedWithClaims(issuer = Constants.SELVBETJENING, claimMap = [Constants.CLAIM_ACR_LEVEL_4])
@RequestMapping("/soknader/{behandlingsId}/personalia/adresser", produces = [MediaType.APPLICATION_JSON_VALUE])
class AdresseRessurs(
    private val tilgangskontroll: Tilgangskontroll,
    private val adresseSystemdata: AdresseSystemdata,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val navEnhetService: NavEnhetService
) {

    private val pabegynteSoknaderOpprettetUnderKsNedetid = listOf(
        "1100239YX",
        "110023A4o",
        "1100239QT",
        "1100239ZS",
        "110023A33",
        "1100239SD",
        "110023A24",
        "110023A3i",
        "1100239ZW",
        "1100239XU",
        "1100239YN",
        "110023A4S",
        "1100239PL",
        "1100239RG",
        "110023A01",
        "110023A55",
    )

    @GetMapping
    fun hentAdresser(
        @PathVariable("behandlingsId") behandlingsId: String
    ): AdresserFrontend {
        tilgangskontroll.verifiserBrukerHarTilgangTilSoknad(behandlingsId)
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)
        val jsonInternalSoknad = soknad.jsonInternalSoknad
            ?: throw IllegalStateException("Kan ikke hente søknaddata hvis SoknadUnderArbeid.jsonInternalSoknad er null")
        val personIdentifikator = jsonInternalSoknad.soknad.data.personalia.personIdentifikator.verdi
        val jsonOppholdsadresse = jsonInternalSoknad.soknad.data.personalia.oppholdsadresse
        val sysFolkeregistrertAdresse = jsonInternalSoknad.soknad.data.personalia.folkeregistrertAdresse
        val sysMidlertidigAdresse = adresseSystemdata.innhentMidlertidigAdresse(personIdentifikator)
        val navEnhet = try {
            navEnhetService.getNavEnhet(
                eier,
                jsonInternalSoknad.soknad,
                jsonInternalSoknad.soknad.data.personalia.oppholdsadresse.adresseValg
            )
        } catch (e: Exception) {
            null
        }
        jsonInternalSoknad.midlertidigAdresse = sysMidlertidigAdresse
        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier)

        // Forsøker å sette mottaker i soknad.json for de påbegynte søknadene i listen med behandlingsId'er over.
        // Noe feilet under KS' nedetid 09.02.23 slik at mottaker aldri ble satt i soknad.json.
        // Uten mottaker, vil vi ikke kunne sende disse søknadene til KS, og dersom bruker skulle forsøke å fortsette på disse påbegynte søknadene vil innsendingen feile.
        if (
            behandlingsId in pabegynteSoknaderOpprettetUnderKsNedetid &&
            navEnhet != null &&
            soknad.jsonInternalSoknad?.mottaker == null &&
            soknad.jsonInternalSoknad?.soknad?.mottaker == null
        ) {
            log.info("Forsøker å sette mottaker i soknad.json for søknad med behandlingsid=$behandlingsId.")
            soknad.jsonInternalSoknad?.mottaker = no.nav.sbl.soknadsosialhjelp.soknad.internal.JsonSoknadsmottaker()
                .withNavEnhetsnavn(NavEnhetUtils.createNavEnhetsnavn(navEnhet.enhetsnavn, navEnhet.kommunenavn))
                .withOrganisasjonsnummer(navEnhet.orgnr)
            soknad.jsonInternalSoknad?.soknad?.mottaker = JsonSoknadsmottaker()
                .withNavEnhetsnavn(NavEnhetUtils.createNavEnhetsnavn(navEnhet.enhetsnavn, navEnhet.kommunenavn))
                .withEnhetsnummer(navEnhet.enhetsnr)
                .withKommunenummer(navEnhet.kommuneNr)
            soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier)
        }

        return AdresseMapper.mapToAdresserFrontend(
            sysFolkeregistrertAdresse,
            sysMidlertidigAdresse,
            jsonOppholdsadresse,
            navEnhet
        )
    }

    @PutMapping
    fun updateAdresse(
        @PathVariable("behandlingsId") behandlingsId: String,
        @RequestBody adresserFrontend: AdresserFrontendInput
    ): List<NavEnhetFrontend>? {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)
        val jsonInternalSoknad = soknad.jsonInternalSoknad
            ?: throw IllegalStateException("Kan ikke oppdatere søknaddata hvis SoknadUnderArbeid.jsonInternalSoknad er null")
        val personalia = jsonInternalSoknad.soknad.data.personalia
        when (adresserFrontend.valg) {
            JsonAdresseValg.FOLKEREGISTRERT ->
                personalia.oppholdsadresse =
                    adresseSystemdata.createDeepCopyOfJsonAdresse(personalia.folkeregistrertAdresse)

            JsonAdresseValg.MIDLERTIDIG ->
                personalia.oppholdsadresse =
                    adresseSystemdata.innhentMidlertidigAdresse(eier)

            JsonAdresseValg.SOKNAD ->
                personalia.oppholdsadresse =
                    adresserFrontend.soknad?.let { AdresseMapper.mapToJsonAdresse(it) }

            else -> throw IllegalStateException("Adressevalg kan ikke være noe annet enn Folkeregistrert, Midlertidig eller Soknad")
        }
        personalia.oppholdsadresse.adresseValg = adresserFrontend.valg
        personalia.postadresse = midlertidigLosningForPostadresse(personalia.oppholdsadresse)
        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier)
        val navEnhetFrontend = navEnhetService.getNavEnhet(
            eier,
            jsonInternalSoknad.soknad,
            adresserFrontend.valg
        )
        return navEnhetFrontend?.let { listOf(it) } ?: emptyList()
    }

    private fun midlertidigLosningForPostadresse(oppholdsadresse: JsonAdresse?): JsonAdresse? {
        if (oppholdsadresse == null) {
            return null
        }
        return if (oppholdsadresse.type == JsonAdresse.Type.MATRIKKELADRESSE) {
            null
        } else adresseSystemdata.createDeepCopyOfJsonAdresse(oppholdsadresse)?.withAdresseValg(null)
    }

    companion object {
        private val log by logger()
    }
}
