package no.nav.sosialhjelp.soknad.personalia.adresse.adresseregister

import no.nav.sosialhjelp.soknad.personalia.adresse.adresseregister.domain.KartverketMatrikkelAdresse
import org.springframework.stereotype.Component

@Component
class HentAdresseService(
    private val hentAdresseClient: HentAdresseClient,
) {
    fun hentKartverketMatrikkelAdresse(matrikkelId: String): KartverketMatrikkelAdresse? {
        return hentAdresseClient.hentMatrikkelAdresse(matrikkelId)
            ?.let {
                KartverketMatrikkelAdresse(
                    kommunenummer = it.matrikkelnummer?.kommunenummer,
                    gaardsnummer = it.matrikkelnummer?.gaardsnummer,
                    bruksnummer = it.matrikkelnummer?.bruksnummer,
                    festenummer = it.matrikkelnummer?.festenummer,
                    seksjonsunmmer = it.matrikkelnummer?.seksjonsnummer,
                    undernummer = it.undernummer,
                    bydelsnummer = it.bydel?.bydelsnummer
                )
            }
    }
}
