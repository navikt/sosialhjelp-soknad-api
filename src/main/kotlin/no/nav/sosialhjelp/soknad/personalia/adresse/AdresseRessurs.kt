package no.nav.sosialhjelp.soknad.personalia.adresse

import io.getunleash.Unleash
import no.nav.sbl.soknadsosialhjelp.soknad.JsonData
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknadsmottaker
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresseValg
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.soknad.ControllerToNewDatamodellProxy
import no.nav.sosialhjelp.soknad.app.Constants
import no.nav.sosialhjelp.soknad.app.MiljoUtils
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.innsending.KortSoknadService
import no.nav.sosialhjelp.soknad.innsending.soknadunderarbeid.SoknadUnderArbeidService
import no.nav.sosialhjelp.soknad.navenhet.NavEnhetService
import no.nav.sosialhjelp.soknad.navenhet.NavEnhetUtils.createNavEnhetsnavn
import no.nav.sosialhjelp.soknad.navenhet.dto.NavEnhetFrontend
import no.nav.sosialhjelp.soknad.personalia.AdresseToNyModellProxy
import no.nav.sosialhjelp.soknad.personalia.adresse.dto.AdresserFrontend
import no.nav.sosialhjelp.soknad.personalia.adresse.dto.AdresserFrontendInput
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken as personId

@RestController
@ProtectedWithClaims(
    issuer = Constants.SELVBETJENING,
    claimMap = [Constants.CLAIM_ACR_LEVEL_4, Constants.CLAIM_ACR_LOA_HIGH],
    combineWithOr = true,
)
@RequestMapping("/soknader/{behandlingsId}/personalia/adresser", produces = [MediaType.APPLICATION_JSON_VALUE])
class AdresseRessurs(
    private val tilgangskontroll: Tilgangskontroll,
    private val adresseSystemdata: AdresseSystemdata,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val navEnhetService: NavEnhetService,
    private val unleash: Unleash,
    private val soknadMetadataRepository: SoknadMetadataRepository,
    private val kortSoknadService: KortSoknadService,
    private val adresseProxy: AdresseToNyModellProxy,
    private val soknadUnderArbeidService: SoknadUnderArbeidService,
) {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @GetMapping
    fun hentAdresser(
        @PathVariable("behandlingsId") behandlingsId: String,
    ): AdresserFrontend {
        // TODO Sjekk logikken mot ny datamodell
        tilgangskontroll.verifiserBrukerHarTilgangTilSoknad(behandlingsId)

        if (ControllerToNewDatamodellProxy.nyDatamodellAktiv) {
            return adresseProxy.getAdresser(behandlingsId)
        } else {
            val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, personId())
            val jsonInternalSoknad =
                soknad.jsonInternalSoknad
                    ?: throw IllegalStateException("Kan ikke hente søknaddata hvis SoknadUnderArbeid.jsonInternalSoknad er null")
            val personIdentifikator = jsonInternalSoknad.soknad.data.personalia.personIdentifikator.verdi
            val jsonOppholdsadresse = jsonInternalSoknad.soknad.data.personalia.oppholdsadresse
            val sysFolkeregistrertAdresse = jsonInternalSoknad.soknad.data.personalia.folkeregistrertAdresse
            val sysMidlertidigAdresse = jsonInternalSoknad.midlertidigAdresse

            // todo skal ikke lagre noe ved get
//        val sysMidlertidigAdresse = adresseSystemdata.innhentMidlertidigAdresseToJsonAdresse(personIdentifikator)

            // TODO Ekstra logging
            logger.info("Hender navEnhet - GET personalia/adresser")
            val navEnhet =
                try {
                    navEnhetService.getNavEnhet(
                        personId(),
                        jsonInternalSoknad.soknad,
                        jsonInternalSoknad.soknad.data.personalia.oppholdsadresse.adresseValg,
                    )
                } catch (e: Exception) {
                    null
                }

            // todo skal ikke lagre noe ved get
//        jsonInternalSoknad.midlertidigAdresse = sysMidlertidigAdresse
//        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, personId())

            return AdresseMapper.mapToAdresserFrontend(
                sysFolkeregistrertAdresse,
                sysMidlertidigAdresse,
                jsonOppholdsadresse,
                navEnhet,
            )
        }
    }

    @PutMapping
    fun updateAdresse(
        @PathVariable("behandlingsId") behandlingsId: String,
        @RequestBody adresserFrontend: AdresserFrontendInput,
    ): List<NavEnhetFrontend>? {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)

        if (ControllerToNewDatamodellProxy.nyDatamodellAktiv) {
            return adresseProxy.updateAdresse(behandlingsId, adresserFrontend)
        } else {
            val personId = personId()

            val token = SubjectHandlerUtils.getToken()
            val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, personId)
            val jsonInternalSoknad =
                soknad.jsonInternalSoknad
                    ?: throw IllegalStateException("Kan ikke oppdatere søknaddata hvis SoknadUnderArbeid.jsonInternalSoknad er null")
            val personalia = jsonInternalSoknad.soknad.data.personalia
            when (adresserFrontend.valg) {
                JsonAdresseValg.FOLKEREGISTRERT ->
                    personalia.oppholdsadresse =
                        adresseSystemdata.createDeepCopyOfJsonAdresse(personalia.folkeregistrertAdresse)

                JsonAdresseValg.MIDLERTIDIG ->
                    personalia.oppholdsadresse =
                        adresseSystemdata.innhentMidlertidigAdresseToJsonAdresse(personId)

                JsonAdresseValg.SOKNAD ->
                    personalia.oppholdsadresse =
                        adresserFrontend.soknad?.let { AdresseMapper.mapToJsonAdresse(it) }

                else -> throw IllegalStateException("Adressevalg kan ikke være noe annet enn Folkeregistrert, Midlertidig eller Soknad")
            }
            personalia.oppholdsadresse?.adresseValg = adresserFrontend.valg
            personalia.postadresse = midlertidigLosningForPostadresse(personalia.oppholdsadresse)

            val navEnhetFrontend =
                navEnhetService.getNavEnhet(
                    personId,
                    jsonInternalSoknad.soknad,
                    adresserFrontend.valg,
                )
            navEnhetFrontend?.let { setNavEnhetAsMottaker(soknad, it, personId) }

            soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, personId)

            navEnhetFrontend?.let { resolveKortSoknad(behandlingsId, it, token) }
                ?: logger.warn("Fant ikke navEnhetFrontend, kan ikke oppdatere kort søknad")

            return navEnhetFrontend?.let { listOf(it) } ?: emptyList()
        }
    }

    // oppdaterer soknadsdata om bruker kvalifiserer for kort soknad
    private fun resolveKortSoknad(
        behandlingsId: String,
        navEnhet: NavEnhetFrontend,
        token: String?,
    ) {
        // Man må skru av og på kort søknad på forsiden i mock/lokalt
        if (!MiljoUtils.isMockAltProfil()) {
            runCatching {
                val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, personId())
                soknad.jsonInternalSoknad ?: error("Finnes ikke jsonInternalSoknad")

                if (token == null) {
                    logger.warn("Token er null, kan ikke sjekke om bruker har rett på kort søknad")
                    return
                }
                val soknadstype = resolveSoknadstype(navEnhet, token)

                soknadUnderArbeidService.updateWithRetries(soknad) {
                    val (changed, isKort) = it.updateSoknadstype(navEnhet, soknadstype)
                    if (changed) {
                        val soknadMetadata = soknadMetadataRepository.hent(behandlingsId)
                        if (soknadMetadata != null) {
                            soknadMetadata.kortSoknad = isKort
                            soknadMetadataRepository.oppdater(soknadMetadata)
                        }
                    }
                }
            }
                .onFailure { error ->
                    logger.error("Noe feilet under kort overgang fra/til kort søknad. Lar det gå uten å røre data.", error)
                }
        }
    }

    // sjekker - muligens eksternt - kom bruker kvalifiserer for kort soknad
    private fun resolveSoknadstype(
        navEnhet: NavEnhetFrontend,
        token: String,
    ): JsonData.Soknadstype {
        val kortSoknad =
            kortSoknadService.isEnabled(navEnhet.kommuneNr) &&
                kortSoknadService.isQualifiedFromFiks(token, navEnhet.kommuneNr ?: "")

        return if (kortSoknad) {
            logger.info("Bruker kvalifiserer til kort søknad")
            JsonData.Soknadstype.KORT
        } else {
            logger.info("Bruker kvalifiserer ikke til kort søknad")
            JsonData.Soknadstype.STANDARD
        }
    }

    /* Sett søknadstype kort om bruker har rett på det. Gjøres her fordi vi må vite hvilken kommune som skal behandle søknaden.
       TODO: Flytt denne logikken til søknadsopprettelse ved full utrulling
       Returnerer true hvis søknadstype ble endret
     */
    private fun JsonInternalSoknad.updateSoknadstype(
        navEnhet: NavEnhetFrontend,
        nySoknadstype: JsonData.Soknadstype,
    ): Pair<Boolean, Boolean> {
        val kortSoknad = (nySoknadstype == JsonData.Soknadstype.KORT)
        if (nySoknadstype != soknad.data.soknadstype) {
            soknad.data.soknadstype = nySoknadstype
            if (nySoknadstype == JsonData.Soknadstype.STANDARD) {
                logger.info("Søknadstype endret fra kort til standard for søknad til ${navEnhet.enhetsnavn}")
                resetKortSoknadFields()
            } else {
                logger.info("Søknadstype endret fra standard til kort for søknad til ${navEnhet.enhetsnavn}")
                vedlegg.vedlegg.addAll(
                    listOf(
                        JsonVedlegg().withType("kort").withTilleggsinfo("behov"),
                        JsonVedlegg().withType("annet").withTilleggsinfo("annet").withStatus("LastetOpp"),
                    ),
                )
                if (!unleash.isEnabled("sosialhjelp.soknad.kategorier")) {
                    soknad.data.begrunnelse.hvaSokesOm = ""
                }
            }
            return true to kortSoknad
        }
        return false to kortSoknad
    }

    fun setNavEnhetAsMottaker(
        soknad: SoknadUnderArbeid,
        navEnhetFrontend: NavEnhetFrontend,
        eier: String,
    ) {
        soknad.jsonInternalSoknad?.mottaker =
            no.nav.sbl.soknadsosialhjelp.soknad.internal
                .JsonSoknadsmottaker()
                .withNavEnhetsnavn(createNavEnhetsnavn(navEnhetFrontend.enhetsnavn, navEnhetFrontend.kommunenavn))
                .withOrganisasjonsnummer(navEnhetFrontend.orgnr)

        soknad.jsonInternalSoknad?.soknad?.mottaker =
            JsonSoknadsmottaker()
                .withNavEnhetsnavn(createNavEnhetsnavn(navEnhetFrontend.enhetsnavn, navEnhetFrontend.kommunenavn))
                .withEnhetsnummer(navEnhetFrontend.enhetsnr)
                .withKommunenummer(navEnhetFrontend.kommuneNr)
    }

    private fun midlertidigLosningForPostadresse(oppholdsadresse: JsonAdresse?): JsonAdresse? {
        if (oppholdsadresse == null) {
            return null
        }
        return if (oppholdsadresse.type == JsonAdresse.Type.MATRIKKELADRESSE) {
            null
        } else {
            adresseSystemdata.createDeepCopyOfJsonAdresse(oppholdsadresse)?.withAdresseValg(null)
        }
    }

    private fun JsonInternalSoknad.resetKortSoknadFields() {
        soknad.data.situasjonendring = null
        vedlegg.vedlegg.removeIf { (it.type == "kort" && it.tilleggsinfo == "behov") || (it.type == "kort" && it.tilleggsinfo == "situasjonsendring") }
        if (!unleash.isEnabled("sosialhjelp.soknad.kategorier")) {
            soknad.data.begrunnelse.hvaSokesOm = ""
        }
    }
}
