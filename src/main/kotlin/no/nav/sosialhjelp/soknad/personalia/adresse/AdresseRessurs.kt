package no.nav.sosialhjelp.soknad.personalia.adresse

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknadsmottaker
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresseValg
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia
import no.nav.sosialhjelp.soknad.app.annotation.ProtectionSelvbetjeningHigh
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.navenhet.NavEnhetService
import no.nav.sosialhjelp.soknad.navenhet.NavEnhetUtils.createNavEnhetsnavn
import no.nav.sosialhjelp.soknad.navenhet.dto.NavEnhetFrontend
import no.nav.sosialhjelp.soknad.personalia.adresse.dto.AdresseFrontend
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
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken as eier

@RestController
@ProtectionSelvbetjeningHigh
@RequestMapping("/soknader/{behandlingsId}/personalia/adresser", produces = [MediaType.APPLICATION_JSON_VALUE])
class AdresseRessurs(
    private val tilgangskontroll: Tilgangskontroll,
    private val adresseSystemdata: AdresseSystemdata,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val navEnhetService: NavEnhetService
) {
    @GetMapping
    fun hentAdresser(
        @PathVariable("behandlingsId") behandlingsId: String
    ): AdresserFrontend {
        tilgangskontroll.verifiserBrukerHarTilgangTilSoknad(behandlingsId)
        val internSoknad = internalFraSoknad(fetchSoknad(behandlingsId))

        return AdresseMapper.mapToAdresserFrontend(
            sysFolkeregistrert = valgtAdresse(internSoknad, JsonAdresseValg.FOLKEREGISTRERT),
            sysMidlertidig = adresseSystemdata.innhentMidlertidigAdresse(eier()),
            jsonOpphold = valgtAdresse(internSoknad, JsonAdresseValg.SOKNAD),
            navEnhet = runCatching { navEnhetService.getNavEnhet(internSoknad.soknad.data.personalia) }.getOrNull()
        )
    }

    @PutMapping
    fun updateAdresse(
        @PathVariable("behandlingsId") behandlingsId: String,
        @RequestBody adresserFrontend: AdresserFrontendInput
    ): List<NavEnhetFrontend>? {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)
        val soknad = fetchSoknad(behandlingsId)
        val internalSoknad = internalFraSoknad(soknad)

        internalSoknad.midlertidigAdresse = adresseSystemdata.innhentMidlertidigAdresse(eier())
        updatePersonalia(internalSoknad.soknad.data.personalia, adresserFrontend.valg, adresserFrontend.soknad)

        val navEnhetFrontend = navEnhetService.getNavEnhet(internalSoknad.soknad.data.personalia)
        setSoknadMottaker(soknad, navEnhetFrontend)

        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier())

        return navEnhetFrontend?.let { listOf(it) } ?: emptyList()
    }

    /**
     * Oppdaterer alt under internalSoknad.soknad.data.personalia
     */
    private fun updatePersonalia(personalia: JsonPersonalia, valg: JsonAdresseValg?, adresse: AdresseFrontend?) {
        personalia.oppholdsadresse = when (valg) {
            JsonAdresseValg.FOLKEREGISTRERT -> adresseSystemdata.createDeepCopyOfJsonAdresse(personalia.folkeregistrertAdresse)
            JsonAdresseValg.MIDLERTIDIG -> adresseSystemdata.innhentMidlertidigAdresse(eier())
            JsonAdresseValg.SOKNAD -> adresse?.let { AdresseMapper.mapToJsonAdresse(it) }
            else -> error("Adressevalg kan ikke være noe annet enn Folkeregistrert, Midlertidig eller Soknad")
        }
        personalia.oppholdsadresse.adresseValg = valg
        personalia.postadresse = midlertidigLosningForPostadresse(personalia.oppholdsadresse)
    }

    fun setSoknadMottaker(
        soknad: SoknadUnderArbeid,
        navEnhetFrontend: NavEnhetFrontend?
    ) {
        if (navEnhetFrontend == null) return
        soknad.jsonInternalSoknad?.mottaker = no.nav.sbl.soknadsosialhjelp.soknad.internal.JsonSoknadsmottaker()
            .withNavEnhetsnavn(createNavEnhetsnavn(navEnhetFrontend.enhetsnavn, navEnhetFrontend.kommunenavn))
            .withOrganisasjonsnummer(navEnhetFrontend.orgnr)

        soknad.jsonInternalSoknad?.soknad?.mottaker = JsonSoknadsmottaker()
            .withNavEnhetsnavn(createNavEnhetsnavn(navEnhetFrontend.enhetsnavn, navEnhetFrontend.kommunenavn))
            .withEnhetsnummer(navEnhetFrontend.enhetsnr)
            .withKommunenummer(navEnhetFrontend.kommuneNr)
    }

    /** Snarvei for SoknadUnderArbeid fra behandlingsId. Sjekker aktiv brukers eierskap. */
    private fun fetchSoknad(behandlingsId: String): SoknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(
        behandlingsId,
        SubjectHandlerUtils.getUserIdFromToken()
    )

    /** Snarvei for å hente it jsonInternalSoknad fra SoknadUnderArbeid og bekrefte at den ikke er null */
    private fun internalFraSoknad(soknad: SoknadUnderArbeid): JsonInternalSoknad =
        soknad.jsonInternalSoknad ?: throw IllegalStateException("Kan ikke oppdatere søknaddata hvis SoknadUnderArbeid.jsonInternalSoknad er null")

    private fun valgtAdresse(soknadIntern: JsonInternalSoknad, valg: JsonAdresseValg): JsonAdresse? =
        when (valg) {
            JsonAdresseValg.FOLKEREGISTRERT -> soknadIntern.soknad.data.personalia.folkeregistrertAdresse
            JsonAdresseValg.MIDLERTIDIG -> soknadIntern.midlertidigAdresse
            JsonAdresseValg.SOKNAD -> soknadIntern.soknad.data.personalia.oppholdsadresse
        }

    private fun midlertidigLosningForPostadresse(oppholdsadresse: JsonAdresse): JsonAdresse? = when {
        oppholdsadresse.type == JsonAdresse.Type.MATRIKKELADRESSE -> null
        else -> adresseSystemdata.createDeepCopyOfJsonAdresse(oppholdsadresse)?.withAdresseValg(null)
    }
}
