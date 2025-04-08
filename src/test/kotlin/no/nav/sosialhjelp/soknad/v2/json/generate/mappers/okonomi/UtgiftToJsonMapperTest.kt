package no.nav.sosialhjelp.soknad.v2.json.generate.mappers.okonomi

import no.nav.sosialhjelp.soknad.v2.json.SoknadJsonTypeEnum
import no.nav.sosialhjelp.soknad.v2.json.generate.mappers.domain.okonomi.UtgiftToJsonMapper
import no.nav.sosialhjelp.soknad.v2.okonomi.AvdragRenter
import no.nav.sosialhjelp.soknad.v2.okonomi.Belop
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiDetaljer
import no.nav.sosialhjelp.soknad.v2.okonomi.Utgift
import no.nav.sosialhjelp.soknad.v2.okonomi.UtgiftType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class UtgiftToJsonMapperTest : AbstractOkonomiMapperTest() {
    @Test
    fun `Utgift med type SFO skal lage JsonOkonomioversiktUtgift`() {
        val utgifter = setOf(Utgift(UtgiftType.UTGIFTER_SFO))

        UtgiftToJsonMapper(utgifter, jsonOkonomi).doMapping()

        with(jsonOkonomi.oversikt) {
            assertThat(utgift).hasSize(1).allMatch { it.type == SoknadJsonTypeEnum.UTGIFTER_SFO.verdi }
        }
    }

    @Test
    fun `Utgift med type STROM skal lage JsonOkonomiopplysningUtgift`() {
        val utgifter = setOf(Utgift(UtgiftType.UTGIFTER_STROM))

        UtgiftToJsonMapper(utgifter, jsonOkonomi).doMapping()

        with(jsonOkonomi.opplysninger) {
            assertThat(utgift).hasSize(1).allMatch { it.type == SoknadJsonTypeEnum.UTGIFTER_STROM.verdi }
        }
    }

    @Test
    fun `Utgift med flere okonomiske detaljer skal gi flere innslag`() {
        val utgifter =
            setOf(
                Utgift(
                    UtgiftType.BARNEBIDRAG_BETALER,
                    null,
                    OkonomiDetaljer(listOf(Belop(444.0), Belop(1242.0))),
                ),
            )
        UtgiftToJsonMapper(utgifter, jsonOkonomi).doMapping()

        with(jsonOkonomi.oversikt) {
            assertThat(utgift).hasSize(2).allMatch { it.type == SoknadJsonTypeEnum.BARNEBIDRAG.verdi }
        }
    }

    @Test
    fun `Beskrivelse for andre boutgifter skal gi flere innslag`() {
        val utgifterDomain = createUtgiftForAnnet(UtgiftType.UTGIFTER_ANNET_BO)

        UtgiftToJsonMapper(utgifterDomain, jsonOkonomi).doMapping()

        jsonOkonomi.opplysninger.utgift.also { utgifter ->
            assertThat(utgifter).hasSize(2)
            assertThat(utgifter).allMatch { it.type == SoknadJsonTypeEnum.UTGIFTER_ANNET_BO.verdi }

            assertThat(utgifter).anyMatch {
                it.belop == belopBeskrivelse1.first.toInt() && it.tittel.contains(belopBeskrivelse1.second)
            }
            assertThat(utgifter).anyMatch {
                it.belop == belopBeskrivelse2.first.toInt() && it.tittel.contains(belopBeskrivelse2.second)
            }
        }
    }

    @Test
    fun `Beskrivelse for andre utgifter barn skal gi flere innslag`() {
        val utgifterDomain = createUtgiftForAnnet(UtgiftType.UTGIFTER_ANNET_BARN)

        UtgiftToJsonMapper(utgifterDomain, jsonOkonomi).doMapping()

        jsonOkonomi.opplysninger.utgift.also { utgifter ->
            assertThat(utgifter).hasSize(2)
            assertThat(utgifter).allMatch { it.type == SoknadJsonTypeEnum.UTGIFTER_ANNET_BARN.verdi }

            assertThat(utgifter).anyMatch {
                it.belop == belopBeskrivelse1.first.toInt() && it.tittel.contains(belopBeskrivelse1.second)
            }
            assertThat(utgifter).anyMatch {
                it.belop == belopBeskrivelse2.first.toInt() && it.tittel.contains(belopBeskrivelse2.second)
            }
        }
    }

    @Test
    fun `Beskrivelse for andre utgifter skal gi flere innslag`() {
        val utgifterDomain = createUtgiftForAnnet(UtgiftType.UTGIFTER_ANDRE_UTGIFTER)

        UtgiftToJsonMapper(utgifterDomain, jsonOkonomi).doMapping()

        jsonOkonomi.opplysninger.utgift.also { utgifter ->
            assertThat(utgifter).hasSize(2)
            assertThat(utgifter).allMatch { it.type == SoknadJsonTypeEnum.UTGIFTER_ANDRE_UTGIFTER.verdi }

            assertThat(utgifter).anyMatch {
                it.belop == belopBeskrivelse1.first.toInt() && it.tittel.contains(belopBeskrivelse1.second)
            }
            assertThat(utgifter).anyMatch {
                it.belop == belopBeskrivelse2.first.toInt() && it.tittel.contains(belopBeskrivelse2.second)
            }
        }
    }

    @Test
    fun `Hver detalj med AvdragRenter skal lage 2 Okonomi-innslag`() {
        val nyUtgift =
            Utgift(
                type = UtgiftType.UTGIFTER_BOLIGLAN,
                beskrivelse = "Boliglan",
                utgiftDetaljer =
                    OkonomiDetaljer(
                        listOf(
                            AvdragRenter(avdrag = 3500.0, renter = 11500.0),
                            AvdragRenter(avdrag = 2500.0, renter = null),
                            AvdragRenter(avdrag = null, renter = 4000.0),
                            AvdragRenter(null, null),
                        ),
                    ),
            )

        UtgiftToJsonMapper(setOf(nyUtgift), jsonOkonomi).doMapping()

        with(jsonOkonomi.oversikt) {
            assertThat(utgift).hasSize(nyUtgift.utgiftDetaljer.detaljer.size * 2)
        }
    }

    @Test
    fun `Midlertidig mapping til gammel type`() {
        val nyUtgift = Utgift(type = UtgiftType.UTGIFTER_STROM)
        val annenUtgift = Utgift(type = UtgiftType.BARNEBIDRAG_BETALER)

        UtgiftToJsonMapper(utgifter = setOf(nyUtgift, annenUtgift), jsonOkonomi).doMapping()

        with(jsonOkonomi) {
            assertThat(opplysninger.utgift).hasSize(1)
            assertThat(opplysninger.utgift.first().type).isEqualTo(SoknadJsonTypeEnum.UTGIFTER_STROM.verdi)

            assertThat(oversikt.utgift).hasSize(1)
            assertThat(oversikt.utgift.first().type).isEqualTo(SoknadJsonTypeEnum.BARNEBIDRAG.verdi)
        }
    }

    private fun createUtgiftForAnnet(utgiftType: UtgiftType): Set<Utgift> {
        return setOf(
            Utgift(
                type = utgiftType,
                utgiftDetaljer =
                    OkonomiDetaljer(
                        listOf(
                            Belop(belop = belopBeskrivelse1.first, beskrivelse = belopBeskrivelse1.second),
                            Belop(belop = belopBeskrivelse2.first, beskrivelse = belopBeskrivelse2.second),
                        ),
                    ),
            ),
        )
    }

    companion object {
        private val belopBeskrivelse1 = Pair(4444.4, "Beskrivelse av første utgift")
        private val belopBeskrivelse2 = Pair(5555.5, "Beskrivelse av andre utgift")
    }
}
