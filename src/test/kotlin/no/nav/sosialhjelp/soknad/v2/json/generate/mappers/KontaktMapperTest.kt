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
import no.nav.sosialhjelp.soknad.v2.kontakt.AdresseValg
import no.nav.sosialhjelp.soknad.v2.kontakt.Adresser
import no.nav.sosialhjelp.soknad.v2.kontakt.NavEnhet
import no.nav.sosialhjelp.soknad.v2.kontakt.Telefonnummer
import no.nav.sosialhjelp.soknad.v2.kontakt.adresse.Adresse
import no.nav.sosialhjelp.soknad.v2.kontakt.adresse.MatrikkelAdresse
import no.nav.sosialhjelp.soknad.v2.kontakt.adresse.UstrukturertAdresse
import no.nav.sosialhjelp.soknad.v2.kontakt.adresse.VegAdresse
import no.nav.sosialhjelp.soknad.v2.opprettKontakt
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.util.UUID

class KontaktMapperTest {
    private val mapper = KontaktToJsonMapper

    @Test
    fun `Kontakt skal mappes til JsonInternalSoknad`() {
        val json = createJsonInternalSoknadWithInitializedSuperObjects()
        val kontakt = opprettKontakt(UUID.randomUUID())

        KontaktToJsonMapper.doMapping(kontakt, json)

        json.assertMidlertidigAdresse(kontakt.adresser.midlertidigAdresse)
        json.assertNavEnhet(kontakt.mottaker)

        with(json.soknad.data.personalia) {
            assertTelefonnummerBruker(kontakt.telefonnummer)
            assertFolkeregistrertAdresse(kontakt.adresser.folkeregistrertAdresse)
            assertOppholdsadresse(kontakt.adresser)
        }
    }
}

private fun JsonPersonalia.assertTelefonnummerBruker(telefonnummer: Telefonnummer) {
    Assertions.assertThat(this.telefonnummer.kilde).isEqualTo(JsonKilde.BRUKER)
    Assertions.assertThat(this.telefonnummer.verdi).isEqualTo(telefonnummer.fraBruker)
}

private fun JsonPersonalia.assertOppholdsadresse(adresser: Adresser) {
    when (adresser.adressevalg) {
        AdresseValg.FOLKEREGISTRERT -> oppholdsadresse.assertAdresse(adresser.folkeregistrertAdresse)
        AdresseValg.MIDLERTIDIG -> oppholdsadresse.assertAdresse(adresser.midlertidigAdresse)
        AdresseValg.SOKNAD -> oppholdsadresse.assertAdresse(adresser.brukerAdresse)
        else -> throw IllegalStateException("AdresseValg ikke satt")
    }
}

private fun JsonInternalSoknad.assertMidlertidigAdresse(midlertidigAdresseSoknad: Adresse?) {
    midlertidigAdresseSoknad?.let {
        midlertidigAdresse.assertAdresse(midlertidigAdresseSoknad)
    }
        ?: Assertions.assertThat(midlertidigAdresse).isNull()
}

private fun JsonPersonalia.assertFolkeregistrertAdresse(folkeregistrertAdresseSoknad: Adresse?) {
    folkeregistrertAdresseSoknad?.let {
        folkeregistrertAdresse.assertAdresse(it)
    }
        ?: Assertions.assertThat(folkeregistrertAdresse).isNull()
}

private fun JsonAdresse.assertAdresse(adresse: Adresse?) {
    Assertions.assertThat(adresse).isNotNull
    when (adresse) {
        is VegAdresse -> (this as JsonGateAdresse).assertAdresse(adresse)
        is MatrikkelAdresse -> (this as JsonMatrikkelAdresse).assertAdresse(adresse)
        is UstrukturertAdresse -> (this as JsonUstrukturertAdresse).assertAdresse(adresse)
    }
}

private fun JsonGateAdresse.assertAdresse(vegAdresse: VegAdresse) {
    Assertions.assertThat(landkode).isEqualTo(vegAdresse.landkode)
    Assertions.assertThat(kommunenummer).isEqualTo(vegAdresse.kommunenummer)
    Assertions.assertThat(adresselinjer).isEqualTo(vegAdresse.adresselinjer)
    Assertions.assertThat(bolignummer).isEqualTo(vegAdresse.bolignummer)
    Assertions.assertThat(postnummer).isEqualTo(vegAdresse.postnummer)
    Assertions.assertThat(poststed).isEqualTo(vegAdresse.poststed)
    Assertions.assertThat(gatenavn).isEqualTo(vegAdresse.gatenavn)
    Assertions.assertThat(husnummer).isEqualTo(vegAdresse.husnummer)
    Assertions.assertThat(husbokstav).isEqualTo(vegAdresse.husbokstav)
}

private fun JsonMatrikkelAdresse.assertAdresse(matrikkelAdresse: MatrikkelAdresse) {
    Assertions.assertThat(kommunenummer).isEqualTo(matrikkelAdresse.kommunenummer)
    Assertions.assertThat(gaardsnummer).isEqualTo(matrikkelAdresse.gaardsnummer)
    Assertions.assertThat(bruksnummer).isEqualTo(matrikkelAdresse.bruksnummer)
    Assertions.assertThat(festenummer).isEqualTo(matrikkelAdresse.festenummer)
    Assertions.assertThat(seksjonsnummer).isEqualTo(matrikkelAdresse.seksjonsnummer)
    Assertions.assertThat(undernummer).isEqualTo(matrikkelAdresse.undernummer)
}

private fun JsonUstrukturertAdresse.assertAdresse(ustrukturertAdresse: UstrukturertAdresse) {
    Assertions.assertThat(adresse).isEqualTo(ustrukturertAdresse.adresse)
}

private fun JsonInternalSoknad.assertNavEnhet(navEnhet: NavEnhet) {
    Assertions.assertThat(mottaker.navEnhetsnavn).isEqualTo(navEnhet.enhetsnavn)
    Assertions.assertThat(mottaker.organisasjonsnummer).isEqualTo(navEnhet.orgnummer)

    Assertions.assertThat(soknad.mottaker.navEnhetsnavn).isEqualTo(navEnhet.enhetsnavn)
    Assertions.assertThat(soknad.mottaker.enhetsnummer).isEqualTo(navEnhet.enhetsnummer)
    Assertions.assertThat(soknad.mottaker.kommunenummer).isEqualTo(navEnhet.kommunenummer)
}
