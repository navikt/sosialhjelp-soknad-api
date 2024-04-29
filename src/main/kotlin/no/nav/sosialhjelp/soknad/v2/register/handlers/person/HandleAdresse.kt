package no.nav.sosialhjelp.soknad.v2.register.handlers.person

import java.util.UUID
import no.nav.sosialhjelp.soknad.personalia.adresse.adresseregister.HentAdresseService
import no.nav.sosialhjelp.soknad.personalia.adresse.adresseregister.domain.KartverketMatrikkelAdresse
import no.nav.sosialhjelp.soknad.personalia.person.domain.Bostedsadresse
import no.nav.sosialhjelp.soknad.personalia.person.domain.Matrikkeladresse
import no.nav.sosialhjelp.soknad.personalia.person.domain.Oppholdsadresse
import no.nav.sosialhjelp.soknad.personalia.person.domain.Person
import no.nav.sosialhjelp.soknad.personalia.person.domain.Vegadresse
import no.nav.sosialhjelp.soknad.v2.kontakt.RegisterDataKontaktService
import no.nav.sosialhjelp.soknad.v2.kontakt.adresse.Adresse
import no.nav.sosialhjelp.soknad.v2.kontakt.adresse.MatrikkelAdresse
import no.nav.sosialhjelp.soknad.v2.kontakt.adresse.VegAdresse
import org.springframework.stereotype.Component

@Component
class HandleAdresse(
    private val kontaktService: RegisterDataKontaktService,
    private val hentAdresseService: HentAdresseService,
): RegisterDataPersonHandler {
    override fun handle(soknadId: UUID, person: Person) {
        kontaktService.saveAdresserRegister(
            soknadId = soknadId,
            folkeregistrert = person.bostedsadresse?.toV2Adresse(hentAdresseService),
            midlertidig = person.oppholdsadresse?.toV2Adresse()
        )
    }
}

fun Bostedsadresse.toV2Adresse(hentAdresseService: HentAdresseService): Adresse? {
    return vegadresse?.toV2VegAdresse() ?: matrikkeladresse?.toV2MatrikkelAdresse(hentAdresseService)
}

fun Oppholdsadresse.toV2Adresse(): Adresse? {
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

private fun Matrikkeladresse.toV2MatrikkelAdresse(hentAdresseService: HentAdresseService): MatrikkelAdresse? {
    return matrikkelId?.let {
        hentAdresseService.hentKartverketMatrikkelAdresse(it)?.toV2MatrikkelAdresse()
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
