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
import no.nav.sosialhjelp.soknad.business.service.soknadservice.Systemdata
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.personalia.person.PersonService
import no.nav.sosialhjelp.soknad.personalia.person.domain.Bostedsadresse
import no.nav.sosialhjelp.soknad.personalia.person.domain.Kontaktadresse
import no.nav.sosialhjelp.soknad.personalia.person.domain.Matrikkeladresse
import no.nav.sosialhjelp.soknad.personalia.person.domain.Oppholdsadresse
import no.nav.sosialhjelp.soknad.personalia.person.domain.Vegadresse

class AdresseSystemdata(
    private val personService: PersonService
) : Systemdata {

    override fun updateSystemdataIn(soknadUnderArbeid: SoknadUnderArbeid) {
        val soknad = soknadUnderArbeid.jsonInternalSoknad.soknad
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
            personalia.postadresse = createDeepCopyOfJsonAdresse(midlertidigAdresse)!!.withAdresseValg(adresseValg)
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
                createDeepCopyOfJsonAdresse(folkeregistrertAdresse)!!.withAdresseValg(adresseValg)
        }
        if (adresseValg == JsonAdresseValg.MIDLERTIDIG) {
            personalia.oppholdsadresse = createDeepCopyOfJsonAdresse(midlertidigAdresse)!!.withAdresseValg(adresseValg)
        }
    }

    fun innhentFolkeregistrertAdresse(personIdentifikator: String?): JsonAdresse? {
        val person = personService.hentPerson(personIdentifikator!!)
        return mapToJsonAdresse(person!!.bostedsadresse)
    }

    fun innhentMidlertidigAdresse(personIdentifikator: String?): JsonAdresse? {
        val person = personService.hentPerson(personIdentifikator!!)
        return mapToJsonAdresse(person!!.oppholdsadresse)
        //        return mapToJsonAdresse(person.getKontaktadresse());
    }

    private fun mapToJsonAdresse(bostedsadresse: Bostedsadresse?): JsonAdresse? {
        if (bostedsadresse == null) {
            return null
        }
        val jsonAdresse: JsonAdresse
        jsonAdresse = if (bostedsadresse.vegadresse != null) {
            tilGateAdresse(bostedsadresse.vegadresse)
        } else if (bostedsadresse.matrikkeladresse != null) {
            tilMatrikkelAdresse(bostedsadresse.matrikkeladresse)
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
        val jsonAdresse: JsonAdresse
        jsonAdresse = if (oppholdsadresse.vegadresse != null) {
            tilGateAdresse(oppholdsadresse.vegadresse)
        } else {
            throw IllegalStateException("Ukjent oppholdsadresse fra PDL (skal være Vegadresse)")
        }
        jsonAdresse.setKilde(JsonKilde.SYSTEM)
        return jsonAdresse
    }

    private fun mapToJsonAdresse(kontaktadresse: Kontaktadresse?): JsonAdresse? {
        if (kontaktadresse == null) {
            return null
        }
        return if (kontaktadresse.vegadresse != null) {
            val jsonAdresse = tilGateAdresse(kontaktadresse.vegadresse)
            jsonAdresse.kilde = JsonKilde.SYSTEM
            jsonAdresse
        } else {
            throw IllegalStateException("Ukjent kontaktadresse fra PDL (skal være Vegadresse)")
        }
    }

    private fun tilGateAdresse(vegadresse: Vegadresse?): JsonGateAdresse {
        val jsonGateAdresse = JsonGateAdresse()
        jsonGateAdresse.type = JsonAdresse.Type.GATEADRESSE
        jsonGateAdresse.landkode = "NOR" // vegadresser er kun norske
        jsonGateAdresse.kommunenummer = vegadresse!!.kommunenummer
        jsonGateAdresse.bolignummer = vegadresse.bruksenhetsnummer
        jsonGateAdresse.gatenavn = vegadresse.adressenavn
        jsonGateAdresse.husnummer = if (vegadresse.husnummer == null) null else vegadresse.husnummer.toString()
        jsonGateAdresse.husbokstav = vegadresse.husbokstav
        jsonGateAdresse.postnummer = vegadresse.postnummer
        jsonGateAdresse.poststed = vegadresse.poststed
        return jsonGateAdresse
    }

    private fun tilMatrikkelAdresse(matrikkeladresse: Matrikkeladresse?): JsonMatrikkelAdresse {
        val jsonMatrikkelAdresse = JsonMatrikkelAdresse()
        jsonMatrikkelAdresse.type = JsonAdresse.Type.MATRIKKELADRESSE
        jsonMatrikkelAdresse.kommunenummer = matrikkeladresse!!.kommunenummer
        jsonMatrikkelAdresse.bruksnummer = matrikkeladresse.bruksenhetsnummer
        return jsonMatrikkelAdresse
    }

    fun createDeepCopyOfJsonAdresse(oppholdsadresse: JsonAdresse?): JsonAdresse? {
        return when (oppholdsadresse!!.type) {
            JsonAdresse.Type.GATEADRESSE -> JsonGateAdresse()
                .withKilde(oppholdsadresse.kilde)
                .withAdresseValg(oppholdsadresse.adresseValg)
                .withType(oppholdsadresse.type)
                .withLandkode((oppholdsadresse as JsonGateAdresse?)!!.landkode)
                .withKommunenummer((oppholdsadresse as JsonGateAdresse?)!!.kommunenummer)
                .withBolignummer((oppholdsadresse as JsonGateAdresse?)!!.bolignummer)
                .withGatenavn((oppholdsadresse as JsonGateAdresse?)!!.gatenavn)
                .withHusnummer((oppholdsadresse as JsonGateAdresse?)!!.husnummer)
                .withHusbokstav((oppholdsadresse as JsonGateAdresse?)!!.husbokstav)
                .withPostnummer((oppholdsadresse as JsonGateAdresse?)!!.postnummer)
                .withPoststed((oppholdsadresse as JsonGateAdresse?)!!.poststed)
            JsonAdresse.Type.MATRIKKELADRESSE -> JsonMatrikkelAdresse()
                .withKilde(oppholdsadresse.kilde)
                .withAdresseValg(oppholdsadresse.adresseValg)
                .withType(oppholdsadresse.type)
                .withKommunenummer((oppholdsadresse as JsonMatrikkelAdresse?)!!.kommunenummer)
                .withGaardsnummer((oppholdsadresse as JsonMatrikkelAdresse?)!!.gaardsnummer)
                .withBruksnummer((oppholdsadresse as JsonMatrikkelAdresse?)!!.bruksnummer)
                .withFestenummer((oppholdsadresse as JsonMatrikkelAdresse?)!!.festenummer)
                .withSeksjonsnummer((oppholdsadresse as JsonMatrikkelAdresse?)!!.seksjonsnummer)
                .withUndernummer((oppholdsadresse as JsonMatrikkelAdresse?)!!.undernummer)
            JsonAdresse.Type.USTRUKTURERT -> JsonUstrukturertAdresse()
                .withKilde(oppholdsadresse.kilde)
                .withAdresseValg(oppholdsadresse.adresseValg)
                .withType(oppholdsadresse.type)
                .withAdresse((oppholdsadresse as JsonUstrukturertAdresse?)!!.adresse)
            JsonAdresse.Type.POSTBOKS -> JsonPostboksAdresse()
                .withKilde(oppholdsadresse.kilde)
                .withAdresseValg(oppholdsadresse.adresseValg)
                .withType(oppholdsadresse.type)
                .withPostboks((oppholdsadresse as JsonPostboksAdresse?)!!.postboks)
                .withPostnummer((oppholdsadresse as JsonPostboksAdresse?)!!.postnummer)
                .withPoststed((oppholdsadresse as JsonPostboksAdresse?)!!.poststed)
            else -> null
        }
    }
}
