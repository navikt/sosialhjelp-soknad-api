package no.nav.sosialhjelp.soknad.nymodell.producer.json.mappers.adresse

import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresseValg
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonGateAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonMatrikkelAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonPostboksAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonUstrukturertAdresse
import no.nav.sosialhjelp.soknad.nymodell.domene.adresse.AdresseObject
import no.nav.sosialhjelp.soknad.nymodell.domene.adresse.AdresseValg
import no.nav.sosialhjelp.soknad.nymodell.domene.adresse.GateAdresseObject
import no.nav.sosialhjelp.soknad.nymodell.domene.adresse.MatrikkelAdresseObject
import no.nav.sosialhjelp.soknad.nymodell.domene.adresse.PostboksAdresseObject
import no.nav.sosialhjelp.soknad.nymodell.domene.adresse.UstrukturertAdresseObject

fun AdresseValg.toJsonAdresseValg(): JsonAdresseValg {
    return JsonAdresseValg.valueOf(this.name)
}

fun AdresseObject.toTypedJsonAdresse(): JsonAdresse {
    return when (this) {
        is GateAdresseObject -> this.toJsonGateAdresse()
        is MatrikkelAdresseObject -> this.toJsonMatrikkelAdresse()
        is PostboksAdresseObject -> this.toJsonPostboksAdresse()
        is UstrukturertAdresseObject -> this.toJsonUstrukturertAdresse()
        else -> throw IllegalStateException("Mapping: AdresseObject-type støttes ikke.")
    }
}

fun GateAdresseObject.toJsonGateAdresse() = JsonGateAdresse()
    .withLandkode(landkode)
    .withKommunenummer(kommunenummer)
    .withAdresselinjer(adresselinjer)
    .withBolignummer(bolignummer)
    .withPostnummer(postnummer)
    .withPoststed(poststed)
    .withGatenavn(gatenavn)
    .withHusnummer(husnummer)
    .withHusbokstav(husbokstav)
    .withType(JsonAdresse.Type.GATEADRESSE)

fun MatrikkelAdresseObject.toJsonMatrikkelAdresse() = JsonMatrikkelAdresse()
    .withKommunenummer(kommunenummer)
    .withGaardsnummer(gaardsnummer)
    .withBruksnummer(bruksnummer)
    .withFestenummer(festenummer)
    .withSeksjonsnummer(seksjonsnummer)
    .withUndernummer(undernummer)
    .withType(JsonAdresse.Type.MATRIKKELADRESSE)

fun PostboksAdresseObject.toJsonPostboksAdresse() = JsonPostboksAdresse()
    .withPostboks(postboks)
    .withPostnummer(postnummer)
    .withPoststed(poststed)
    .withType(JsonAdresse.Type.POSTBOKS)

fun UstrukturertAdresseObject.toJsonUstrukturertAdresse() = JsonUstrukturertAdresse()
    .withAdresse(adresse)
    .withType(JsonAdresse.Type.USTRUKTURERT)
