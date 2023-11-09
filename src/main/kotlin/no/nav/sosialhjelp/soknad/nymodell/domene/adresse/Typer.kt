package no.nav.sosialhjelp.soknad.nymodell.domene.adresse

import no.nav.sosialhjelp.soknad.nymodell.domene.Kilde

enum class AdresseValg(val kilde: Kilde) {
    FOLKEREGISTRERT(Kilde.SYSTEM),
    MIDLERTIDIG(Kilde.SYSTEM),
    SOKNAD(Kilde.BRUKER);
}

enum class AdresseType {
    GATEADRESSE,
    MATRIKKELADRESSE,
    POSTBOKSADRESSE,
    USTRUKTURERT;
}