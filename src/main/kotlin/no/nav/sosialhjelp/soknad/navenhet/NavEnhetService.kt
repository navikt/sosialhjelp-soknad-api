package no.nav.sosialhjelp.soknad.navenhet

import io.getunleash.Unleash
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresseValg
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonGateAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonMatrikkelAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia
import no.nav.sosialhjelp.soknad.adressesok.domain.AdresseForslag
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.MiljoUtils
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneInfoService
import no.nav.sosialhjelp.soknad.kodeverk.KodeverkService
import no.nav.sosialhjelp.soknad.navenhet.bydel.BydelFordelingService
import no.nav.sosialhjelp.soknad.navenhet.domain.NavEnhet
import no.nav.sosialhjelp.soknad.navenhet.dto.NavEnhetFrontend
import no.nav.sosialhjelp.soknad.navenhet.finnadresse.FinnAdresseService
import no.nav.sosialhjelp.soknad.navenhet.gt.GeografiskTilknytningService
import org.springframework.stereotype.Component

@Component
class NavEnhetService(
    private val norgService: NorgService,
    private val kommuneInfoService: KommuneInfoService,
    private val bydelFordelingService: BydelFordelingService,
    private val finnAdresseService: FinnAdresseService,
    private val geografiskTilknytningService: GeografiskTilknytningService,
    private val kodeverkService: KodeverkService,
    private val unleash: Unleash,
) {
    fun getNavEnhet(
        eier: String,
        soknad: JsonSoknad,
        valg: JsonAdresseValg?,
    ): NavEnhetFrontend? {
        val personalia = soknad.data.personalia
        return if (JsonAdresseValg.FOLKEREGISTRERT == valg) {
            try {
                val kommunenummer = validerKommunenummerVedFolkeregistrertValgt(personalia)
                finnNavEnhetFraGT(eier, kommunenummer)
            } catch (exception: Exception) {
                log.warn(
                    "Noe feilet henting av NavEnhet fra GT -> fallback til adressesøk for vegadresse / hentAdresse for matrikkeladresse",
                    exception,
                )
                finnNavEnhetFraAdresse(personalia, valg)
            }
        } else {
            finnNavEnhetFraAdresse(personalia, valg)
        }
    }

    internal fun validerKommunenummerVedFolkeregistrertValgt(personalia: JsonPersonalia): String? {
        return runCatching {
            val fraFolkeregistrert = getKommunenummer(personalia.folkeregistrertAdresse)
            val fraOpphold = getKommunenummer(personalia.oppholdsadresse)

            if (fraFolkeregistrert == fraOpphold) {
                fraOpphold
            } else {
                log.error(
                    "Ved adressevalg Folkeregistrert, er kommunenummer for" +
                        "Oppholdsadrese: $fraOpphold og Folkeregistrert: $fraFolkeregistrert",
                )
                fraFolkeregistrert
            }
        }
            .onFailure { log.error("Feil ved sammenlikning av kommunenummer", it) }
            .getOrNull()
    }

    private fun finnNavEnhetFraGT(
        ident: String,
        kommunenummer: String?,
    ): NavEnhetFrontend? {
        log.info("Finner Nav-enhet fra GT")

        // gt er 4 sifret kommunenummer eller 6 sifret bydelsnummer
        val geografiskTilknytning = geografiskTilknytningService.hentGeografiskTilknytning(ident)
        val navEnhet = norgService.getEnhetForGt(geografiskTilknytning)

        return mapToNavEnhetFrontend(navEnhet, geografiskTilknytning, kommunenummer)
    }

    private fun finnNavEnhetFraAdresse(
        personalia: JsonPersonalia,
        valg: JsonAdresseValg?,
    ): NavEnhetFrontend? {
        log.info("Finner Nav-enhet fra adresse")

        val adresseForslag = finnAdresseService.finnAdresseFraSoknad(personalia, valg) ?: return null
        val geografiskTilknytning = getGeografiskTilknytningFromAdresseForslag(adresseForslag)
        val navEnhet = norgService.getEnhetForGt(geografiskTilknytning)

        return mapToNavEnhetFrontend(navEnhet, geografiskTilknytning, adresseForslag.kommunenummer)
    }

    private fun mapToNavEnhetFrontend(
        navEnhet: NavEnhet?,
        geografiskTilknytning: String?,
        kommunenummer: String?,
    ): NavEnhetFrontend? {
        if (navEnhet == null) {
            log.warn("Kunne ikke hente NAV-enhet: $geografiskTilknytning , i kommune: $kommunenummer")
            return null
        }
        if (kommunenummer == null || kommunenummer.length != 4) {
            log.warn("Kommunenummer hadde ikke 4 tegn, var $kommunenummer")
            return null
        }
        val isDigisosKommune = kanMottaSoknader(kommunenummer)
        val sosialOrgnr = navEnhet.sosialOrgNr.takeIf { isDigisosKommune }
        val enhetNr = navEnhet.enhetNr.takeIf { isDigisosKommune }
        val kommunenavn = kodeverkService.getKommunenavn(kommunenummer)
        return NavEnhetFrontend(
            enhetsnr = enhetNr,
            enhetsnavn = navEnhet.navn,
            kommunenavn = kommuneInfoService.getBehandlingskommune(kommunenummer, kommunenavn),
            orgnr = sosialOrgnr,
            valgt = enhetNr != null,
            kommuneNr = kommunenummer,
            isMottakDeaktivert = !isDigisosKommune,
            isMottakMidlertidigDeaktivert = kommuneInfoService.harMidlertidigDeaktivertMottak(kommunenummer),
        )
    }

    private fun getKommunenummer(oppholdsadresse: JsonAdresse?): String? {
        if (oppholdsadresse == null) return null

        if (
            MiljoUtils.isNonProduction() &&
            unleash.isEnabled(FEATURE_SEND_TIL_NAV_TESTKOMMUNE, false) &&
            oppholdsadresse.adresseValg == JsonAdresseValg.FOLKEREGISTRERT
        ) {
            log.error("Sender til Nav-testkommune (3002). Du skal aldri se denne meldingen i PROD")
            return "3002"
        }

        return when (oppholdsadresse) {
            is JsonMatrikkelAdresse -> oppholdsadresse.kommunenummer
            is JsonGateAdresse -> oppholdsadresse.kommunenummer
            else -> null
        }
    }

    private fun kanMottaSoknader(kommunenummer: String): Boolean {
        val isNyDigisosApiKommuneMedMottakAktivert = kommuneInfoService.kanMottaSoknader(kommunenummer)
        return isNyDigisosApiKommuneMedMottakAktivert
    }

    private fun getGeografiskTilknytningFromAdresseForslag(adresseForslag: AdresseForslag): String? {
        return if (BydelFordelingService.BYDEL_MARKA_OSLO == adresseForslag.geografiskTilknytning) {
            bydelFordelingService.getBydelTilForMarka(adresseForslag)
        } else {
            // flere special cases her?
            adresseForslag.geografiskTilknytning
        }
    }

    companion object {
        private val log by logger()
        const val FEATURE_SEND_TIL_NAV_TESTKOMMUNE = "sosialhjelp.soknad.send-til-nav-testkommune"
    }
}
