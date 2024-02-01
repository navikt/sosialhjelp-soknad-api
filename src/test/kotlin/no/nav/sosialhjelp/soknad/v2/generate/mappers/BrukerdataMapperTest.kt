package no.nav.sosialhjelp.soknad.v2.generate.mappers

import no.nav.sbl.soknadsosialhjelp.soknad.JsonData
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibeskrivelserAvAnnet
import no.nav.sosialhjelp.soknad.v2.brukerdata.Brukerdata
import no.nav.sosialhjelp.soknad.v2.brukerdata.Samtykke
import no.nav.sosialhjelp.soknad.v2.createJsonInternalSoknadWithInitializedSuperObjects
import no.nav.sosialhjelp.soknad.v2.generate.mappers.domain.BrukerdataToJsonMapper
import no.nav.sosialhjelp.soknad.v2.generate.mappers.domain.BrukerdataToJsonMapper.BrukerdataMapper.toSoknadJsonType
import no.nav.sosialhjelp.soknad.v2.opprettBrukerdata
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.*

class BrukerdataMapperTest {

    private val mapper = BrukerdataToJsonMapper.BrukerdataMapper

    @Test
    fun `Brukerdata mappes til JsonInternalSoknad`() {
        val json = createJsonInternalSoknadWithInitializedSuperObjects()

        val brukerdata = opprettBrukerdata(soknadId = UUID.randomUUID())
        mapper.doMapping(brukerdata, json)

        json.soknad.data
            .run {
                assertKilder()
                assertPersonalia(brukerdata)
                assertArbeid(brukerdata)
                assertBegrunnelse(brukerdata)
                okonomi.opplysninger.assertSamtykker(brukerdata)
                okonomi.opplysninger.beskrivelseAvAnnet.assertBeskrivelserAvAnnet(brukerdata)
            }
    }

    private fun JsonData.assertKilder() {
        personalia.kontonummer
            ?.let { assertThat(it.kilde).isEqualTo(JsonKilde.BRUKER) }
        personalia.telefonnummer
            ?.let { assertThat(it.kilde).isEqualTo(JsonKilde.BRUKER) }
        arbeid.kommentarTilArbeidsforhold
            ?.let { assertThat(it.kilde).isEqualTo(JsonKildeBruker.BRUKER) }
        begrunnelse
            ?.let { assertThat(it.kilde).isEqualTo(JsonKildeBruker.BRUKER) }
        with(okonomi.opplysninger) {
            bekreftelse
                .forEach { assertThat(it.kilde).isEqualTo(JsonKilde.BRUKER) }
            beskrivelseAvAnnet
                ?.let { assertThat(it.kilde).isEqualTo(JsonKildeBruker.BRUKER) }
        }
    }

    private fun JsonData.assertPersonalia(brukerdata: Brukerdata) {
        brukerdata.telefonnummer?.let {
            assertThat(personalia.telefonnummer.verdi).isEqualTo(it)
        }
            ?: assertThat(personalia.telefonnummer).isNull()

        if (brukerdata.kontoInformasjon != null) {

            brukerdata.kontoInformasjon?.kontonummer?.let {
                assertThat(it).isEqualTo(personalia.kontonummer.verdi)
            }
                ?: assertThat(personalia.kontonummer.verdi).isNull()

            brukerdata.kontoInformasjon?.harIkkeKonto?.let {
                assertThat(it).isEqualTo(personalia.kontonummer.harIkkeKonto)
            }
                ?: Assertions.assertNull(personalia.kontonummer.harIkkeKonto)
        } else {
            assertThat(personalia.kontonummer).isNull()
        }
    }

    private fun JsonData.assertArbeid(brukerdata: Brukerdata) {
        brukerdata.kommentarArbeidsforhold?.let {
            assertThat(arbeid.kommentarTilArbeidsforhold.verdi).isEqualTo(it)
        }
            ?: assertThat(arbeid.kommentarTilArbeidsforhold).isNull()
    }

    private fun JsonData.assertBegrunnelse(brukerdata: Brukerdata) {
        brukerdata.begrunnelse?.let {
            assertThat(begrunnelse.hvaSokesOm).isEqualTo(it.hvaSokesOm)
            assertThat(begrunnelse.hvorforSoke).isEqualTo(it.hvorforSoke)
        }
            ?: assertThat(begrunnelse).isNull()
    }

    private fun JsonOkonomiopplysninger.assertSamtykker(brukerdata: Brukerdata) {
        brukerdata.samtykker.forEach {
            it.assertBekreftelse(bekreftelse)
        }
    }

    private fun Samtykke.assertBekreftelse(bekreftelser: List<JsonOkonomibekreftelse>) {
        val soknadJsonType = type.toSoknadJsonType()
        val bekreftelse = bekreftelser.find { it.type == soknadJsonType }

        bekreftelse?.let {
            assertThat(type.tittel).isEqualTo(it.tittel)
            verdi?.let { domainVerdi -> assertThat(domainVerdi).isEqualTo(it.verdi) }
                ?: Assertions.assertNull(it.verdi)

            assertThat(dato.toString()).isEqualTo(it.bekreftelsesDato)
        }
            ?: throw RuntimeException("Bekreftelse er null")
    }

    private fun JsonOkonomibeskrivelserAvAnnet.assertBeskrivelserAvAnnet(brukerdata: Brukerdata) {
        brukerdata.beskrivelseAvAnnet?.let {
            assertThat(verdi).isEqualTo(it.verdier)
            assertThat(sparing).isEqualTo(it.sparing)
            assertThat(utbetaling).isEqualTo(it.utbetalinger)
            assertThat(boutgifter).isEqualTo(it.boutgifter)
            assertThat(barneutgifter).isEqualTo(it.barneutgifter)
        }
    }
}
