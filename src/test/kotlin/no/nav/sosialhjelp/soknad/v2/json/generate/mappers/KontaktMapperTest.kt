package no.nav.sosialhjelp.soknad.v2.json.generate.mappers

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonGateAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonMatrikkelAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonUstrukturertAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia
import no.nav.sosialhjelp.soknad.v2.createJsonInternalSoknadWithInitializedSuperObjects
import no.nav.sosialhjelp.soknad.v2.json.generate.mappers.domain.KontaktToJsonMapper
import no.nav.sosialhjelp.soknad.v2.kontakt.Adresse
import no.nav.sosialhjelp.soknad.v2.kontakt.AdresseValg
import no.nav.sosialhjelp.soknad.v2.kontakt.Adresser
import no.nav.sosialhjelp.soknad.v2.kontakt.MatrikkelAdresse
import no.nav.sosialhjelp.soknad.v2.kontakt.NavEnhet
import no.nav.sosialhjelp.soknad.v2.kontakt.Telefonnummer
import no.nav.sosialhjelp.soknad.v2.kontakt.UstrukturertAdresse
import no.nav.sosialhjelp.soknad.v2.kontakt.VegAdresse
import no.nav.sosialhjelp.soknad.v2.opprettKontakt
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

class KontaktMapperTest {
    private val mapper = KontaktToJsonMapper

    @Test
    fun `Kontakt skal mappes til JsonInternalSoknad`() {
        val json = createJsonInternalSoknadWithInitializedSuperObjects()
        val kontakt = opprettKontakt(UUID.randomUUID())

        mapper.doMapping(kontakt, json)

        json.assertMidlertidigAdresse(kontakt.adresser.midlertidig)
        json.assertNavEnhet(kontakt.mottaker)

        with(json.soknad.data.personalia) {
            assertTelefonnummerBruker(kontakt.telefonnummer)
            assertFolkeregistrertAdresse(kontakt.adresser.folkeregistrert)
            assertOppholdsadresse(kontakt.adresser)
        }
    }
}

private fun JsonPersonalia.assertTelefonnummerBruker(telefonnummer: Telefonnummer) {
    assertThat(this.telefonnummer.kilde).isEqualTo(JsonKilde.BRUKER)
    assertThat(this.telefonnummer.verdi).isEqualTo(telefonnummer.fraBruker)
}

private fun JsonPersonalia.assertOppholdsadresse(adresser: Adresser) {
    when (adresser.adressevalg) {
        AdresseValg.FOLKEREGISTRERT -> oppholdsadresse.assertAdresse(adresser.folkeregistrert)
        AdresseValg.MIDLERTIDIG -> oppholdsadresse.assertAdresse(adresser.midlertidig)
        AdresseValg.SOKNAD -> oppholdsadresse.assertAdresse(adresser.fraBruker)
        else -> throw IllegalStateException("AdresseValg ikke satt")
    }
}

private fun JsonInternalSoknad.assertMidlertidigAdresse(midlertidigAdresseSoknad: Adresse?) {
    midlertidigAdresseSoknad?.let {
        midlertidigAdresse.assertAdresse(midlertidigAdresseSoknad)
    }
        ?: assertThat(midlertidigAdresse).isNull()
}

private fun JsonPersonalia.assertFolkeregistrertAdresse(folkeregistrertAdresseSoknad: Adresse?) {
    folkeregistrertAdresseSoknad?.let {
        folkeregistrertAdresse.assertAdresse(it)
    }
        ?: assertThat(folkeregistrertAdresse).isNull()
}

private fun JsonAdresse.assertAdresse(adresse: Adresse?) {
    assertThat(adresse).isNotNull
    when (adresse) {
        is VegAdresse -> (this as JsonGateAdresse).assertAdresse(adresse)
        is MatrikkelAdresse -> (this as JsonMatrikkelAdresse).assertAdresse(adresse)
        is UstrukturertAdresse -> (this as JsonUstrukturertAdresse).assertAdresse(adresse)
    }
}

private fun JsonGateAdresse.assertAdresse(vegAdresse: VegAdresse) {
    assertThat(landkode).isEqualTo(vegAdresse.landkode)
    assertThat(kommunenummer).isEqualTo(vegAdresse.kommunenummer)
    assertThat(adresselinjer).isEqualTo(vegAdresse.adresselinjer)
    assertThat(bolignummer).isEqualTo(vegAdresse.bolignummer)
    assertThat(postnummer).isEqualTo(vegAdresse.postnummer)
    assertThat(poststed).isEqualTo(vegAdresse.poststed)
    assertThat(gatenavn).isEqualTo(vegAdresse.gatenavn)
    assertThat(husnummer).isEqualTo(vegAdresse.husnummer)
    assertThat(husbokstav).isEqualTo(vegAdresse.husbokstav)
}

private fun JsonMatrikkelAdresse.assertAdresse(matrikkelAdresse: MatrikkelAdresse) {
    assertThat(kommunenummer).isEqualTo(matrikkelAdresse.kommunenummer)
    assertThat(gaardsnummer).isEqualTo(matrikkelAdresse.gaardsnummer)
    assertThat(bruksnummer).isEqualTo(matrikkelAdresse.bruksnummer)
    assertThat(festenummer).isEqualTo(matrikkelAdresse.festenummer)
    assertThat(seksjonsnummer).isEqualTo(matrikkelAdresse.seksjonsnummer)
    assertThat(undernummer).isEqualTo(matrikkelAdresse.undernummer)
}

private fun JsonUstrukturertAdresse.assertAdresse(ustrukturertAdresse: UstrukturertAdresse) {
    assertThat(adresse).isEqualTo(ustrukturertAdresse.adresse)
}

private fun JsonInternalSoknad.assertNavEnhet(navEnhet: NavEnhet) {
    assertThat(mottaker.navEnhetsnavn).isEqualTo("${navEnhet.enhetsnavn}, ${navEnhet.kommunenavn}")
    assertThat(mottaker.organisasjonsnummer).isEqualTo(navEnhet.orgnummer)

    assertThat(soknad.mottaker.navEnhetsnavn).isEqualTo("${navEnhet.enhetsnavn}, ${navEnhet.kommunenavn}")
    assertThat(soknad.mottaker.enhetsnummer).isEqualTo(navEnhet.enhetsnummer)
    assertThat(soknad.mottaker.kommunenummer).isEqualTo(navEnhet.kommunenummer)
}
