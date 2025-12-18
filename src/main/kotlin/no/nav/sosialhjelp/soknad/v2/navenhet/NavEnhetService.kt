package no.nav.sosialhjelp.soknad.v2.navenhet

import no.nav.sosialhjelp.soknad.adressesok.domain.AdresseForslag
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.kodeverk.KodeverkService
import no.nav.sosialhjelp.soknad.navenhet.NorgService
import no.nav.sosialhjelp.soknad.navenhet.bydel.BydelFordelingService
import no.nav.sosialhjelp.soknad.navenhet.bydel.BydelFordelingService.Companion.BYDEL_MARKA_OSLO
import no.nav.sosialhjelp.soknad.navenhet.finnadresse.FinnAdresseService
import no.nav.sosialhjelp.soknad.v2.kontakt.Adresse
import no.nav.sosialhjelp.soknad.v2.kontakt.NavEnhet
import org.springframework.stereotype.Service

@Service
class NavEnhetService(
    private val finnAdresseService: FinnAdresseService,
    private val norgService: NorgService,
    private val bydelFordelingService: BydelFordelingService,
    private val kodeverkService: KodeverkService,
) {
    fun getNavEnhet(
        adresse: Adresse,
    ): NavEnhet? = finnNavEnhetFraAdresse(adresse)

    @Deprecated("Finn direkte fra adresse uten FinnAdresseService etter 14(19) dager")
    private fun finnNavEnhetFraAdresse(
        adresse: Adresse,
    ): NavEnhet {
        val adresseForslag = finnAdresseService.finnAdresseFraSoknad(adresse) ?: error("Fant ikke adresseforslag")

        val gt = adresseForslag.getGeografiskTilknytning()
        val navEnhet = norgService.getEnhetForGt(gt) ?: error("Fant ingen Nav-enhet for gt: $gt")
        val kommunenavn = adresseForslag.kommunenummer?.let { getKommunenavn(it) }

        log.info("Fant Nav-enhet ${navEnhet.enhetsnavn} (${navEnhet.enhetsnummer}) for gt: $gt")

        return navEnhet.copy(
            kommunenummer = adresseForslag.kommunenummer,
            kommunenavn = kommunenavn,
        )
    }

    private fun getKommunenavn(kommunenummer: String): String? = kodeverkService.getKommunenavn(kommunenummer)

    private fun AdresseForslag.getGeografiskTilknytning(): String =
        when (BYDEL_MARKA_OSLO == geografiskTilknytning) {
            true -> bydelFordelingService.getBydelTilForMarka(this)
            false -> geografiskTilknytning
        }
            ?: error("AdresseForslag mangler geografisk tilknytning")

    companion object {
        private val log by logger()
    }
}
