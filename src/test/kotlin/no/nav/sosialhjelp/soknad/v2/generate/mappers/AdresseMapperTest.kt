package no.nav.sosialhjelp.soknad.v2.generate.mappers

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonGateAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonMatrikkelAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonUstrukturertAdresse
import no.nav.sosialhjelp.soknad.v2.adresse.Adresse
import no.nav.sosialhjelp.soknad.v2.adresse.AdresserSoknad
import no.nav.sosialhjelp.soknad.v2.adresse.MatrikkelAdresse
import no.nav.sosialhjelp.soknad.v2.adresse.UstrukturertAdresse
import no.nav.sosialhjelp.soknad.v2.adresse.VegAdresse
import no.nav.sosialhjelp.soknad.v2.brukerdata.AdresseValg
import no.nav.sosialhjelp.soknad.v2.createJsonInternalSoknadWithInitializedSuperObjects
import no.nav.sosialhjelp.soknad.v2.generate.mappers.domain.AdresseToJsonMapper
import no.nav.sosialhjelp.soknad.v2.opprettAdresserSoknad
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.*

class AdresseMapperTest {

    private val mapper = AdresseToJsonMapper.AdresseMapper

    @Test
    fun `Adresse-data skal mappes til JsonInternalSoknad`() {

        val json = createJsonInternalSoknadWithInitializedSuperObjects()
        val adresserSoknad = opprettAdresserSoknad(UUID.randomUUID())

        mapper.doMapping(adresserSoknad, json)

        with(json) {
            assertMidlertidigAdresse(adresserSoknad.midlertidigAdresse)
            assertFolkeregistrertAdresse(adresserSoknad.folkeregistrertAdresse)
            assertOppholdsadresse(adresserSoknad)
        }
    }
}

private fun JsonInternalSoknad.assertOppholdsadresse(adresserSoknad: AdresserSoknad) {
    val jsonOppholdsAdresse = soknad.data.personalia.oppholdsadresse

    adresserSoknad.brukerInput?.let {
        when (adresserSoknad.brukerInput?.valgtAdresse) {
            AdresseValg.FOLKEREGISTRERT -> jsonOppholdsAdresse.assertAdresse(adresserSoknad.folkeregistrertAdresse)
            AdresseValg.MIDLERTIDIG -> jsonOppholdsAdresse.assertAdresse(adresserSoknad.midlertidigAdresse)
            AdresseValg.SOKNAD -> jsonOppholdsAdresse.assertAdresse(it.brukerAdresse)
            else -> throw IllegalStateException("Adressetype ikke stottet")
        }
    } ?: assertThat(jsonOppholdsAdresse).isNull()
}

private fun JsonInternalSoknad.assertMidlertidigAdresse(midlertidigAdresseSoknad: Adresse?) {
    midlertidigAdresseSoknad?.let {
        midlertidigAdresse.assertAdresse(it)
    }
        ?: assertThat(midlertidigAdresse).isNull()
}

private fun JsonInternalSoknad.assertFolkeregistrertAdresse(folkeregistrertAdresseSoknad: Adresse?) {
    folkeregistrertAdresseSoknad?.let {
        soknad.data.personalia.folkeregistrertAdresse.assertAdresse(it)
    }
        ?: assertThat(soknad.data.personalia.folkeregistrertAdresse).isNull()
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
