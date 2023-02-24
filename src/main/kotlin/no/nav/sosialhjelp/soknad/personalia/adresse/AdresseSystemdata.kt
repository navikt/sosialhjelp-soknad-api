package no.nav.sosialhjelp.soknad.personalia.adresse

import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknadsmottaker
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresseValg
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonGateAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonMatrikkelAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonPostboksAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonUstrukturertAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia
import no.nav.sosialhjelp.soknad.app.systemdata.Systemdata
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.personalia.adresse.adresseregister.HentAdresseService
import no.nav.sosialhjelp.soknad.personalia.adresse.adresseregister.domain.KartverketMatrikkelAdresse
import no.nav.sosialhjelp.soknad.personalia.person.PersonService
import no.nav.sosialhjelp.soknad.personalia.person.domain.Bostedsadresse
import no.nav.sosialhjelp.soknad.personalia.person.domain.Matrikkeladresse
import no.nav.sosialhjelp.soknad.personalia.person.domain.Oppholdsadresse
import no.nav.sosialhjelp.soknad.personalia.person.domain.Vegadresse
import org.springframework.stereotype.Component

@Component
class AdresseSystemdata(
    private val personService: PersonService,
    private val hentAdresseService: HentAdresseService
) : Systemdata {

    override fun updateSystemdataIn(soknadUnderArbeid: SoknadUnderArbeid) {
        val soknad = soknadUnderArbeid.jsonInternalSoknad?.soknad ?: return

        val personalia = soknad.data.personalia
        val personIdentifikator = personalia.personIdentifikator.verdi
        val folkeregistrertAdresse = innhentFolkeregistrertAdresse(personIdentifikator)
        val midlertidigAdresse = innhentMidlertidigAdresse(personIdentifikator)
        if (valgtAdresseLikNull(personalia, folkeregistrertAdresse, midlertidigAdresse)) {
            personalia.oppholdsadresse = null
            personalia.postadresse = null
            soknad.mottaker = JsonSoknadsmottaker()
        }
        personalia.folkeregistrertAdresse = folkeregistrertAdresse
        updateOppholdsadresse(personalia, folkeregistrertAdresse, midlertidigAdresse)
        updatePostadresse(personalia, folkeregistrertAdresse, midlertidigAdresse)
    }

    private fun valgtAdresseLikNull(
        personalia: JsonPersonalia,
        folkeregistrertAdresse: JsonAdresse?,
        midlertidigAdresse: JsonAdresse?
    ): Boolean {
        return (
            folkeregistrertAdresse == null && personalia.oppholdsadresse != null && JsonAdresseValg.FOLKEREGISTRERT == personalia.oppholdsadresse.adresseValg ||
                midlertidigAdresse == null && personalia.oppholdsadresse != null && JsonAdresseValg.MIDLERTIDIG == personalia.oppholdsadresse.adresseValg
            )
    }

    private fun updatePostadresse(
        personalia: JsonPersonalia,
        folkeregistrertAdresse: JsonAdresse?,
        midlertidigAdresse: JsonAdresse?
    ) {
        val postadresse = personalia.postadresse ?: return
        val adresseValg = postadresse.adresseValg
        if (adresseValg == JsonAdresseValg.FOLKEREGISTRERT) {
            personalia.postadresse = createDeepCopyOfJsonAdresse(folkeregistrertAdresse)
        }
        if (adresseValg == JsonAdresseValg.MIDLERTIDIG) {
            personalia.postadresse = createDeepCopyOfJsonAdresse(midlertidigAdresse)?.withAdresseValg(adresseValg)
        }
    }

    private fun updateOppholdsadresse(
        personalia: JsonPersonalia,
        folkeregistrertAdresse: JsonAdresse?,
        midlertidigAdresse: JsonAdresse?
    ) {
        val oppholdsadresse = personalia.oppholdsadresse ?: return
        val adresseValg = oppholdsadresse.adresseValg
        if (adresseValg == JsonAdresseValg.FOLKEREGISTRERT) {
            personalia.oppholdsadresse =
                createDeepCopyOfJsonAdresse(folkeregistrertAdresse)?.withAdresseValg(adresseValg)
        }
        if (adresseValg == JsonAdresseValg.MIDLERTIDIG) {
            personalia.oppholdsadresse = createDeepCopyOfJsonAdresse(midlertidigAdresse)?.withAdresseValg(adresseValg)
        }
    }

    private fun innhentFolkeregistrertAdresse(personIdentifikator: String): JsonAdresse? {
        return personService.hentPerson(personIdentifikator)?.bostedsadresse.let { mapToJsonAdresse(it) }
    }

    fun innhentMidlertidigAdresse(personIdentifikator: String): JsonAdresse? {
        return personService.hentPerson(personIdentifikator)?.oppholdsadresse.let { mapToJsonAdresse(it) }
    }

    private fun hentMatrikkelAdresseFraKartverket(matrikkelId: String): KartverketMatrikkelAdresse? {
        return hentAdresseService.hentKartverketMatrikkelAdresse(matrikkelId)
    }

    private fun mapToJsonAdresse(bostedsadresse: Bostedsadresse?): JsonAdresse? {
        if (bostedsadresse == null) {
            return null
        }
        val jsonAdresse: JsonAdresse = if (bostedsadresse.vegadresse != null) {
            tilGateAdresse(bostedsadresse.vegadresse)
        } else if (bostedsadresse.matrikkeladresse != null) {
            val matrikkelId: String? = bostedsadresse.matrikkeladresse.matrikkelId
            matrikkelId
                ?.let { hentMatrikkelAdresseFraKartverket(it) }
                ?.let { mapToJsonMatrikkelAdresse(it) }
                ?: tilMatrikkelAdresse(bostedsadresse.matrikkeladresse)
        } else {
            throw IllegalStateException("Ukjent bostedsadresse fra PDL (skal være Vegadresse eller Matrikkeladresse")
        }
        jsonAdresse.kilde = JsonKilde.SYSTEM
        return jsonAdresse
    }

    private fun mapToJsonAdresse(oppholdsadresse: Oppholdsadresse?): JsonAdresse? {
        if (oppholdsadresse == null) {
            return null
        }
        val jsonAdresse: JsonAdresse = if (oppholdsadresse.vegadresse != null) {
            tilGateAdresse(oppholdsadresse.vegadresse)
        } else {
            throw IllegalStateException("Ukjent oppholdsadresse fra PDL (skal være Vegadresse)")
        }
        jsonAdresse.kilde = JsonKilde.SYSTEM
        return jsonAdresse
    }

    private fun tilGateAdresse(vegadresse: Vegadresse): JsonGateAdresse {
        val jsonGateAdresse = JsonGateAdresse()
        jsonGateAdresse.type = JsonAdresse.Type.GATEADRESSE
        jsonGateAdresse.landkode = "NOR" // vegadresser er kun norske
        jsonGateAdresse.kommunenummer = vegadresse.kommunenummer
        jsonGateAdresse.bolignummer = vegadresse.bruksenhetsnummer
        jsonGateAdresse.gatenavn = vegadresse.adressenavn
        jsonGateAdresse.husnummer = vegadresse.husnummer?.toString()
        jsonGateAdresse.husbokstav = vegadresse.husbokstav
        jsonGateAdresse.postnummer = vegadresse.postnummer
        jsonGateAdresse.poststed = vegadresse.poststed
        return jsonGateAdresse
    }

    private fun tilMatrikkelAdresse(matrikkeladresse: Matrikkeladresse): JsonMatrikkelAdresse {
        val jsonMatrikkelAdresse = JsonMatrikkelAdresse()
        jsonMatrikkelAdresse.type = JsonAdresse.Type.MATRIKKELADRESSE
        jsonMatrikkelAdresse.kommunenummer = matrikkeladresse.kommunenummer
        jsonMatrikkelAdresse.bruksnummer = matrikkeladresse.bruksenhetsnummer
        return jsonMatrikkelAdresse
    }

    private fun mapToJsonMatrikkelAdresse(kartverketMatrikkelAdresse: KartverketMatrikkelAdresse): JsonMatrikkelAdresse {
        val jsonMatrikkelAdresse = JsonMatrikkelAdresse()
        jsonMatrikkelAdresse.type = JsonAdresse.Type.MATRIKKELADRESSE
        jsonMatrikkelAdresse.kommunenummer = kartverketMatrikkelAdresse.kommunenummer
        jsonMatrikkelAdresse.gaardsnummer = kartverketMatrikkelAdresse.gaardsnummer
        jsonMatrikkelAdresse.bruksnummer = kartverketMatrikkelAdresse.bruksnummer
        jsonMatrikkelAdresse.festenummer = kartverketMatrikkelAdresse.festenummer
        jsonMatrikkelAdresse.seksjonsnummer = kartverketMatrikkelAdresse.seksjonsunmmer
        jsonMatrikkelAdresse.undernummer = kartverketMatrikkelAdresse.undernummer
        return jsonMatrikkelAdresse
    }

    fun createDeepCopyOfJsonAdresse(oppholdsadresse: JsonAdresse?): JsonAdresse? {
        if (oppholdsadresse == null) {
            return null
        }
        return when (oppholdsadresse.type) {
            JsonAdresse.Type.GATEADRESSE -> {
                val gateadresse = oppholdsadresse as JsonGateAdresse
                JsonGateAdresse()
                    .withKilde(gateadresse.kilde)
                    .withAdresseValg(gateadresse.adresseValg)
                    .withType(gateadresse.type)
                    .withLandkode(gateadresse.landkode)
                    .withKommunenummer(gateadresse.kommunenummer)
                    .withBolignummer(gateadresse.bolignummer)
                    .withGatenavn(gateadresse.gatenavn)
                    .withHusnummer(gateadresse.husnummer)
                    .withHusbokstav(gateadresse.husbokstav)
                    .withPostnummer(gateadresse.postnummer)
                    .withPoststed(gateadresse.poststed)
            }
            JsonAdresse.Type.MATRIKKELADRESSE -> {
                val matrikkeladresse = oppholdsadresse as JsonMatrikkelAdresse
                JsonMatrikkelAdresse()
                    .withKilde(matrikkeladresse.kilde)
                    .withAdresseValg(matrikkeladresse.adresseValg)
                    .withType(matrikkeladresse.type)
                    .withKommunenummer(matrikkeladresse.kommunenummer)
                    .withGaardsnummer(matrikkeladresse.gaardsnummer)
                    .withBruksnummer(matrikkeladresse.bruksnummer)
                    .withFestenummer(matrikkeladresse.festenummer)
                    .withSeksjonsnummer(matrikkeladresse.seksjonsnummer)
                    .withUndernummer(matrikkeladresse.undernummer)
            }
            JsonAdresse.Type.USTRUKTURERT -> {
                val ustrukturertAdresse = oppholdsadresse as JsonUstrukturertAdresse
                JsonUstrukturertAdresse()
                    .withKilde(ustrukturertAdresse.kilde)
                    .withAdresseValg(ustrukturertAdresse.adresseValg)
                    .withType(ustrukturertAdresse.type)
                    .withAdresse(ustrukturertAdresse.adresse)
            }
            JsonAdresse.Type.POSTBOKS -> {
                val postboksadresse = oppholdsadresse as JsonPostboksAdresse
                JsonPostboksAdresse()
                    .withKilde(postboksadresse.kilde)
                    .withAdresseValg(postboksadresse.adresseValg)
                    .withType(postboksadresse.type)
                    .withPostboks(postboksadresse.postboks)
                    .withPostnummer(postboksadresse.postnummer)
                    .withPoststed(postboksadresse.poststed)
            }
            else -> null
        }
    }
}
