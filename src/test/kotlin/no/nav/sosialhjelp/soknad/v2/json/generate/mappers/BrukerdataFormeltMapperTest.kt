package no.nav.sosialhjelp.soknad.v2.json.generate.mappers

import no.nav.sbl.soknadsosialhjelp.soknad.JsonData
import no.nav.sbl.soknadsosialhjelp.soknad.bosituasjon.JsonBosituasjon
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibeskrivelserAvAnnet
import no.nav.sbl.soknadsosialhjelp.soknad.utdanning.JsonUtdanning
import no.nav.sosialhjelp.soknad.v2.brukerdata.BrukerdataFormelt
import no.nav.sosialhjelp.soknad.v2.brukerdata.BrukerdataPerson
import no.nav.sosialhjelp.soknad.v2.brukerdata.Samtykke
import no.nav.sosialhjelp.soknad.v2.createJsonInternalSoknadWithInitializedSuperObjects
import no.nav.sosialhjelp.soknad.v2.json.generate.mappers.domain.BrukerdataToJsonMapper
import no.nav.sosialhjelp.soknad.v2.json.generate.mappers.domain.BrukerdataToJsonMapper.BrukerdataMapper.toSoknadJsonType
import no.nav.sosialhjelp.soknad.v2.opprettBrukerdataFormelt
import no.nav.sosialhjelp.soknad.v2.opprettBrukerdataPerson
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.*

class BrukerdataFormeltMapperTest {

    private val mapper = BrukerdataToJsonMapper.BrukerdataMapper

    @Test
    fun `Brukerdata skal mappes til JsonInternalSoknad`() {
        val json = createJsonInternalSoknadWithInitializedSuperObjects()

        val soknadId = UUID.randomUUID()

        val brukerdataFormelt = opprettBrukerdataFormelt(soknadId)
        val brukerdataPersonlig = opprettBrukerdataPerson(soknadId)

        mapper.doMapping(brukerdataPersonlig, json)
        mapper.doMapping(brukerdataFormelt, json)

        json.soknad.data
            .run {
                assertKilder()
                assertPersonalia(brukerdataPersonlig)
                assertBegrunnelse(brukerdataPersonlig)
                assertBosituasjon(brukerdataPersonlig)

                assertUtdanning(brukerdataFormelt)
                assertArbeid(brukerdataFormelt)
                okonomi.opplysninger.assertSamtykker(brukerdataFormelt)
                okonomi.opplysninger.beskrivelseAvAnnet.assertBeskrivelserAvAnnet(brukerdataFormelt)
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
        utdanning
            ?.let { assertThat(it.kilde).isEqualTo(JsonKilde.BRUKER) }
        bosituasjon
            ?.let { assertThat(it.kilde).isEqualTo(JsonKildeBruker.BRUKER) }
        with(okonomi.opplysninger) {
            bekreftelse
                .forEach { assertThat(it.kilde).isEqualTo(JsonKilde.BRUKER) }
            beskrivelseAvAnnet
                ?.let { assertThat(it.kilde).isEqualTo(JsonKildeBruker.BRUKER) }
        }
    }

    private fun JsonData.assertPersonalia(brukerdataPerson: BrukerdataPerson) {
        brukerdataPerson.telefonnummer?.let {
            assertThat(personalia.telefonnummer.verdi).isEqualTo(it)
        }
            ?: assertThat(personalia.telefonnummer).isNull()

        if (brukerdataPerson.kontoInformasjon != null) {

            brukerdataPerson.kontoInformasjon?.kontonummer?.let {
                assertThat(it).isEqualTo(personalia.kontonummer.verdi)
            }
                ?: assertThat(personalia.kontonummer.verdi).isNull()

            brukerdataPerson.kontoInformasjon?.harIkkeKonto?.let {
                assertThat(it).isEqualTo(personalia.kontonummer.harIkkeKonto)
            }
                ?: Assertions.assertNull(personalia.kontonummer.harIkkeKonto)
        } else {
            assertThat(personalia.kontonummer).isNull()
        }
    }

    private fun JsonData.assertArbeid(brukerdataFormelt: BrukerdataFormelt) {
        brukerdataFormelt.kommentarArbeidsforhold?.let {
            assertThat(arbeid.kommentarTilArbeidsforhold.verdi).isEqualTo(it)
        }
            ?: assertThat(arbeid.kommentarTilArbeidsforhold).isNull()
    }

    private fun JsonData.assertBegrunnelse(brukerdataPerson: BrukerdataPerson) {
        brukerdataPerson.begrunnelse?.let {
            assertThat(begrunnelse.hvaSokesOm).isEqualTo(it.hvaSokesOm)
            assertThat(begrunnelse.hvorforSoke).isEqualTo(it.hvorforSoke)
        }
            ?: assertThat(begrunnelse).isNull()
    }

    private fun JsonOkonomiopplysninger.assertSamtykker(brukerdataFormelt: BrukerdataFormelt) {
        brukerdataFormelt.samtykker.forEach {
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

    private fun JsonOkonomibeskrivelserAvAnnet.assertBeskrivelserAvAnnet(brukerdataFormelt: BrukerdataFormelt) {
        brukerdataFormelt.beskrivelseAvAnnet?.let {
            assertThat(verdi).isEqualTo(it.verdier)
            assertThat(sparing).isEqualTo(it.sparing)
            assertThat(utbetaling).isEqualTo(it.utbetalinger)
            assertThat(boutgifter).isEqualTo(it.boutgifter)
            assertThat(barneutgifter).isEqualTo(it.barneutgifter)
        }
    }

    private fun JsonData.assertUtdanning(brukerdataFormelt: BrukerdataFormelt) {
        brukerdataFormelt.utdanning?.let {
            assertThat(it.erStudent).isEqualTo(utdanning.erStudent)

            assertThat(JsonUtdanning.Studentgrad.fromValue(it.studentGrad?.name?.lowercase()))
                .isEqualTo(utdanning.studentgrad)
        }
            ?: assertThat(utdanning).isNull()
    }

    private fun JsonData.assertBosituasjon(brukerdataPerson: BrukerdataPerson) {
        brukerdataPerson.bosituasjon?.let {
            assertThat(JsonBosituasjon.Botype.fromValue(it.botype?.name?.lowercase()))
                .isEqualTo(bosituasjon.botype)

            assertThat(it.antallHusstand).isEqualTo(bosituasjon.antallPersoner)
        }
            ?: assertThat(bosituasjon).isNull()
    }
}
