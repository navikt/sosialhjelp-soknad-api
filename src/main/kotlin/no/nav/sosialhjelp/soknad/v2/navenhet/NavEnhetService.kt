package no.nav.sosialhjelp.soknad.v2.navenhet

import io.getunleash.Unleash
import no.nav.sosialhjelp.soknad.adressesok.domain.AdresseForslag
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.MiljoUtils
import no.nav.sosialhjelp.soknad.navenhet.NavEnhetService.Companion.FEATURE_SEND_TIL_NAV_TESTKOMMUNE
import no.nav.sosialhjelp.soknad.navenhet.NorgService
import no.nav.sosialhjelp.soknad.navenhet.bydel.BydelFordelingService
import no.nav.sosialhjelp.soknad.navenhet.finnadresse.FinnAdresseService
import no.nav.sosialhjelp.soknad.navenhet.gt.GeografiskTilknytningService
import no.nav.sosialhjelp.soknad.v2.kontakt.Adresse
import no.nav.sosialhjelp.soknad.v2.kontakt.AdresseValg
import no.nav.sosialhjelp.soknad.v2.kontakt.MatrikkelAdresse
import no.nav.sosialhjelp.soknad.v2.kontakt.NavEnhet
import no.nav.sosialhjelp.soknad.v2.kontakt.VegAdresse
import org.springframework.stereotype.Component

@Component("nyNavEnhetService")
class NavEnhetService(
    private val finnAdresseService: FinnAdresseService,
    private val unleash: Unleash,
    private val geografiskTilknytningService: GeografiskTilknytningService,
    private val norgService: NorgService,
    private val bydelFordelingService: BydelFordelingService,
) {
    private val log by logger()

    fun getNavEnhet(
        eier: String,
        adresse: Adresse,
        valg: AdresseValg?,
    ): NavEnhet? {
        if (valg == AdresseValg.FOLKEREGISTRERT) {
            runCatching { return finnNavEnhetFraGT(eier, getKommunenummer(adresse)) }
                .onFailure { log.error("Kunne ikke hente NavEnhet fra GT", it) }
        }
        log.info("Finner Nav-enhet fra adressesøk")
        return finnNavEnhetFraAdresse(adresse)
    }

    private fun getKommunenummer(adresse: Adresse?): String? =
        when {
            MiljoUtils.isNonProduction() &&
                unleash.isEnabled(FEATURE_SEND_TIL_NAV_TESTKOMMUNE, false) -> {
                log.error("Sender til Nav-testkommune (3002). Du skal aldri se denne meldingen i prod")
                "3002"
            }
            adresse is VegAdresse -> {
                adresse.kommunenummer
            }
            adresse is MatrikkelAdresse -> {
                adresse.kommunenummer
            }
            else -> {
                null
            }
        }

    private fun finnNavEnhetFraGT(
        ident: String,
        kommunenummer: String?,
    ): NavEnhet {
        log.info("Finner Nav-enhet fra GT")
        // gt er 4 sifret kommunenummer eller 6 sifret bydelsnummer
        val geografiskTilknytning = geografiskTilknytningService.hentGeografiskTilknytning(ident)
        val navEnhet = norgService.getEnhetForGt(geografiskTilknytning)

        return NavEnhet(navEnhet?.navn, navEnhet?.enhetNr, kommunenummer, navEnhet?.sosialOrgNr, navEnhet?.kommunenavn)
    }

    private fun finnNavEnhetFraAdresse(
        adresse: Adresse,
    ): NavEnhet? {
        log.info("Finner Nav-enhet fra adresse")
        val adresseForslag = finnAdresseService.finnAdresseFraSoknad(adresse) ?: return null
        val geografiskTilknytning = getGeografiskTilknytningFromAdresseForslag(adresseForslag)
        val navEnhet = norgService.getEnhetForGt(geografiskTilknytning)

        return NavEnhet(navEnhet?.navn, navEnhet?.enhetNr, adresseForslag.kommunenummer, navEnhet?.sosialOrgNr, navEnhet?.kommunenavn)
    }

    private fun getGeografiskTilknytningFromAdresseForslag(adresseForslag: AdresseForslag): String? =
        if (BydelFordelingService.BYDEL_MARKA_OSLO == adresseForslag.geografiskTilknytning) {
            bydelFordelingService.getBydelTilForMarka(adresseForslag)
        } else {
            // flere special cases her?
            adresseForslag.geografiskTilknytning
        }
}
