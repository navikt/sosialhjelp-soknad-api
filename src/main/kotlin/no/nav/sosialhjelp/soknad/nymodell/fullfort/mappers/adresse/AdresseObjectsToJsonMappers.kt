//package no.nav.sosialhjelp.soknad.nymodell.fullfort.mappers.adresse
//
//import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse
//import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresseValg
//import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonGateAdresse
//import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonMatrikkelAdresse
//import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonPostboksAdresse
//import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonUstrukturertAdresse
//import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
//import no.nav.sosialhjelp.soknad.nymodell.domene.adresse.Adresse
//import no.nav.sosialhjelp.soknad.nymodell.domene.adresse.AdresseType.*
//import no.nav.sosialhjelp.soknad.nymodell.domene.adresse.GateAdresseObject
//import no.nav.sosialhjelp.soknad.nymodell.domene.adresse.MatrikkelAdresseObject
//import no.nav.sosialhjelp.soknad.nymodell.domene.adresse.PostboksAdresseObject
//import no.nav.sosialhjelp.soknad.nymodell.domene.adresse.UstrukturertAdresseObject
//
//fun Adresse.toTypedJsonAdresse(): JsonAdresse {
//    val jsonAdresse = when (adresseType) {
//        GATEADRESSE -> (adresseObject as GateAdresseObject).toJsonGateAdresse()
//        MATRIKKELADRESSE -> (adresseObject as MatrikkelAdresseObject).toJsonMatrikkelAdresse()
//        POSTBOKSADRESSE -> (adresseObject as PostboksAdresseObject).toJsonPostboksAdresse()
//        USTRUKTURERT -> (adresseObject as UstrukturertAdresseObject).toJsonUstrukturertAdresse()
//    }
//
//    return jsonAdresse.apply {
//        kilde = toJsonKilde()
//        type = toAdresseType()
//    }
//}
//
//fun Adresse.toJsonKilde() = JsonKilde.valueOf(id.typeAdressevalg.kilde.name)
//
//fun Adresse.toAdresseValg() = JsonAdresseValg.valueOf(id.typeAdressevalg.name)
//
//fun Adresse.toAdresseType() = JsonAdresse.Type.valueOf(adresseType.name)
//
//fun GateAdresseObject.toJsonGateAdresse() = JsonGateAdresse()
//    .withLandkode(landkode)
//    .withKommunenummer(kommunenummer)
//    .withAdresselinjer(adresselinjer)
//    .withBolignummer(bolignummer)
//    .withPostnummer(postnummer)
//    .withPoststed(poststed)
//    .withGatenavn(gatenavn)
//    .withHusnummer(husnummer)
//    .withHusbokstav(husbokstav)
//
//fun MatrikkelAdresseObject.toJsonMatrikkelAdresse() = JsonMatrikkelAdresse()
//    .withKommunenummer(kommunenummer)
//    .withGaardsnummer(gaardsnummer)
//    .withBruksnummer(bruksnummer)
//    .withFestenummer(festenummer)
//    .withSeksjonsnummer(seksjonsnummer)
//    .withUndernummer(undernummer)
//
//fun PostboksAdresseObject.toJsonPostboksAdresse() = JsonPostboksAdresse()
//    .withPostboks(postboks)
//    .withPostnummer(postnummer)
//    .withPoststed(poststed)
//
//fun UstrukturertAdresseObject.toJsonUstrukturertAdresse() = JsonUstrukturertAdresse()
//    .withAdresse(adresse)