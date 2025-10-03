package no.nav.sosialhjelp.soknad.v2.json.generate.mappers.okonomi

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.v2.createFormuer
import no.nav.sosialhjelp.soknad.v2.json.SoknadJsonTypeEnum
import no.nav.sosialhjelp.soknad.v2.json.generate.mappers.domain.okonomi.FormueToJsonMapper
import no.nav.sosialhjelp.soknad.v2.okonomi.Belop
import no.nav.sosialhjelp.soknad.v2.okonomi.Formue
import no.nav.sosialhjelp.soknad.v2.okonomi.FormueType
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiDetaljer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class FormueToJsonMapperTest : AbstractOkonomiMapperTest() {
    @Test
    fun `Formue skal mappes til JsonOkonomioversiktFormue`() {
        val formuer = createFormuer()

        FormueToJsonMapper(formuer, jsonOkonomi).doMapping()

        with(jsonOkonomi.oversikt) {
            assertThat(formue).hasSize(2)
            assertThat(formue)
                .anyMatch { it.type == SoknadJsonTypeEnum.FORMUE_BRUKSKONTO.verdi }
                .anyMatch { it.type == SoknadJsonTypeEnum.VERDI_KJORETOY.verdi }
        }
    }

    @Test
    fun `Formue med flere rader skal generere flere JsonOkonomioversiktFormue-innslag`() {
        val formuer = setOf(Formue(type = FormueType.FORMUE_BRUKSKONTO, formueDetaljer = createOkonomiskeDetaljer()))

        FormueToJsonMapper(formuer, jsonOkonomi).doMapping()

        with(jsonOkonomi.oversikt) {
            assertThat(formue)
                .hasSize(3)
                .allMatch { it.type == SoknadJsonTypeEnum.FORMUE_BRUKSKONTO.verdi }
                .anyMatch { it.belop == 344 }
                .anyMatch { it.belop == 244 }
                .anyMatch { it.belop == 644 }
        }
    }

    @Test
    fun `FormueType ANNET skal lagre beskrivelse i JsonBeskrivelserAvAnnet`() {
        val formuer =
            setOf(
                Formue(
                    type = FormueType.FORMUE_ANNET,
                    beskrivelse = "Beskrivelse av Formue",
                    formueDetaljer = OkonomiDetaljer(listOf(Belop(423.0), Belop(288.0))),
                ),
                Formue(
                    type = FormueType.VERDI_ANNET,
                    beskrivelse = "Beskrivelse av Verdi",
                    formueDetaljer = OkonomiDetaljer(listOf(Belop(523.0), Belop(121.0))),
                ),
            )
        FormueToJsonMapper(formuer, jsonOkonomi).doMapping()

        with(jsonOkonomi) {
            assertThat(oversikt.formue)
                .hasSize(4)
                .anyMatch { it.belop == 423 }
                .anyMatch { it.belop == 288 }
                .anyMatch { it.belop == 523 }
                .anyMatch { it.belop == 121 }
                .allMatch {
                    it.type == SoknadJsonTypeEnum.FORMUE_ANNET.verdi || it.type == SoknadJsonTypeEnum.VERDI_ANNET.verdi
                }

            assertThat(opplysninger.beskrivelseAvAnnet.sparing)
                .isEqualTo(formuer.find { it.type == FormueType.FORMUE_ANNET }?.beskrivelse)

            assertThat(opplysninger.beskrivelseAvAnnet.verdi)
                .isEqualTo(formuer.find { it.type == FormueType.VERDI_ANNET }?.beskrivelse)
        }
    }

    @Test
    fun `Midlertidig mapping til gammel type`() {
        val nyFormue = Formue(type = FormueType.FORMUE_BRUKSKONTO)
        val annenFormue = Formue(type = FormueType.VERDI_BOLIG)

        FormueToJsonMapper(formuer = setOf(nyFormue, annenFormue), jsonOkonomi).doMapping()

        with(jsonOkonomi.oversikt) {
            assertThat(formue).hasSize(2)
            assertThat(formue)
                .anyMatch { it.type == SoknadJsonTypeEnum.FORMUE_BRUKSKONTO.verdi }
                .anyMatch { it.type == SoknadJsonTypeEnum.VERDI_BOLIG.verdi }
        }
    }

    @Test
    fun whatever() {
        runBlocking {
            val job =
                launch(Dispatchers.IO) {
                    logger.info("Launching a new scope in ${Thread.currentThread().name}")
                    delay(1000)
                    logger.info("Launch done")
                }

            job.join()

            val deffered =
                async(Dispatchers.Default) {
                    logger.info("Calculating in ${Thread.currentThread().name}")
                    delay(2000)
                    42
                }

            logger.info("Calculation is ${deffered.await()}")

            withContext(Dispatchers.Unconfined) {
                logger.info("With context in ${Thread.currentThread().name}")
                delay(500)
                logger.info("With context done in ${Thread.currentThread().name}")
            }

            val a = 4
        }
    }

    companion object {
        val logger by logger()
    }
}

private fun createOkonomiskeDetaljer(): OkonomiDetaljer<Belop> =
    OkonomiDetaljer(
        detaljer =
            listOf(
                Belop(344.0),
                Belop(244.0),
                Belop(644.0),
            ),
    )
