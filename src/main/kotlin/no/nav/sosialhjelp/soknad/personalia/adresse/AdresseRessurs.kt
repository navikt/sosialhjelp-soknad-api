package no.nav.sosialhjelp.soknad.personalia.adresse

import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresseValg
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.soknad.app.Constants
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.navenhet.NavEnhetService
import no.nav.sosialhjelp.soknad.navenhet.NavEnhetUtils.createNavEnhetsnavn
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
        )?.also { setMottaker(soknad, it, eier) }
        return navEnhetFrontend?.let { listOf(it) } ?: emptyList()
    }

    private fun setMottaker(
        soknad: SoknadUnderArbeid,
        it: NavEnhetFrontend,
        eier: String,
    ) {
        soknad.jsonInternalSoknad?.mottaker = no.nav.sbl.soknadsosialhjelp.soknad.internal.JsonSoknadsmottaker()
            .withNavEnhetsnavn(createNavEnhetsnavn(it.enhetsnavn, it.kommunenavn))
            .withOrganisasjonsnummer(it.orgnr)
        soknad.jsonInternalSoknad?.soknad?.mottaker = no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknadsmottaker()
            .withNavEnhetsnavn(createNavEnhetsnavn(it.enhetsnavn, it.kommunenavn))
            .withEnhetsnummer(it.enhetsnr)
            .withKommunenummer(it.kommuneNr)
        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier)
    }

    private fun midlertidigLosningForPostadresse(oppholdsadresse: JsonAdresse?): JsonAdresse? {
        if (oppholdsadresse == null) {
            return null
        }
        return if (oppholdsadresse.type == JsonAdresse.Type.MATRIKKELADRESSE) {
            null
        } else adresseSystemdata.createDeepCopyOfJsonAdresse(oppholdsadresse)?.withAdresseValg(null)
    }
}
