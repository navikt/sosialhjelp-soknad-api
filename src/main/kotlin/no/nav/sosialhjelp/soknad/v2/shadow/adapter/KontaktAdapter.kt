package no.nav.sosialhjelp.soknad.v2.shadow.adapter

import no.nav.sosialhjelp.soknad.personalia.adresse.adresseregister.HentAdresseService
import no.nav.sosialhjelp.soknad.personalia.adresse.adresseregister.domain.KartverketMatrikkelAdresse
import no.nav.sosialhjelp.soknad.personalia.person.domain.Bostedsadresse
import no.nav.sosialhjelp.soknad.personalia.person.domain.Matrikkeladresse
import no.nav.sosialhjelp.soknad.personalia.person.domain.Oppholdsadresse
import no.nav.sosialhjelp.soknad.personalia.person.domain.Vegadresse
import no.nav.sosialhjelp.soknad.v2.kontakt.Adresser
import no.nav.sosialhjelp.soknad.v2.kontakt.Kontakt
import no.nav.sosialhjelp.soknad.v2.kontakt.KontaktRepository
import no.nav.sosialhjelp.soknad.v2.kontakt.Telefonnummer
import no.nav.sosialhjelp.soknad.v2.kontakt.adresse.Adresse
import no.nav.sosialhjelp.soknad.v2.kontakt.adresse.MatrikkelAdresse
import no.nav.sosialhjelp.soknad.v2.kontakt.adresse.VegAdresse
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import java.util.*

@Component
class KontaktAdapter(
    private val kontaktRepository: KontaktRepository,
    private val hentAdresseService: HentAdresseService
) {
    fun saveAdresser(soknadId: UUID, bostedsadresse: Bostedsadresse?, oppholdsadresse: Oppholdsadresse?) {
        getEntity(soknadId).copy(
            adresser = Adresser(
                folkeregistrertAdresse = bostedsadresse?.toV2Adresse(),
                midlertidigAdresse = oppholdsadresse?.toV2Adresse(),
            )
        ).also { kontaktRepository.save(it) }
    }

    fun addTelefonnummerRegister(soknadId: UUID, telefonnummer: String) {
        getEntity(soknadId).copy(
            telefonnummer = Telefonnummer(register = telefonnummer)
        ).also { kontaktRepository.save(it) }
    }

    private fun getEntity(soknadId: UUID) = kontaktRepository
        .findByIdOrNull(soknadId) ?: Kontakt(soknadId)

    private fun Bostedsadresse.toV2Adresse(): Adresse? {
        return vegadresse?.toV2VegAdresse()
            ?: matrikkeladresse?.toV2MatrikkelAdresse()
    }

    private fun Oppholdsadresse.toV2Adresse(): Adresse? {
        return vegadresse?.toV2VegAdresse()
    }

    private fun Vegadresse.toV2VegAdresse(): VegAdresse {
        return VegAdresse(
            landkode = "NOR",
            kommunenummer = this.kommunenummer,
            bolignummer = this.bruksenhetsnummer,
            postnummer = this.postnummer,
            poststed = this.poststed,
            gatenavn = this.adressenavn,
            husnummer = this.husnummer.toString(),
            husbokstav = this.husbokstav,
        )
    }

    private fun Matrikkeladresse.toV2MatrikkelAdresse(): MatrikkelAdresse? {
        return matrikkelId?.let {

            hentAdresseService.hentKartverketMatrikkelAdresse(it)
                ?.toV2MatrikkelAdresse()
        }
    }

    private fun KartverketMatrikkelAdresse.toV2MatrikkelAdresse(): MatrikkelAdresse {
        return MatrikkelAdresse(
            kommunenummer = kommunenummer!!,
            gaardsnummer = gaardsnummer!!,
            bruksnummer = bruksnummer!!,
            festenummer = festenummer,
            seksjonsnummer = seksjonsnummer,
            undernummer = undernummer,
        )
    }
}
