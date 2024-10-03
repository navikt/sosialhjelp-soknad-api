package no.nav.sosialhjelp.soknad.v2.json.generate.mappers.okonomi

import no.nav.sbl.soknadsosialhjelp.soknad.bostotte.JsonBostotteSak
import no.nav.sosialhjelp.soknad.v2.createBostotteSaker
import no.nav.sosialhjelp.soknad.v2.json.OpplysningTypeMapper
import no.nav.sosialhjelp.soknad.v2.json.generate.mappers.domain.okonomi.BostotteSakToJsonMapper
import no.nav.sosialhjelp.soknad.v2.okonomi.BostotteStatus
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.InntektType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class BostotteSakToJsonMapperTest : AbstractOkonomiMapperTest() {
    @Test
    fun `Bostottesaker skal mappes til tilsvarende innslag i JsonBostotteSaker`() {
        val bostotteSaker = createBostotteSaker()

        BostotteSakToJsonMapper(bostotteSaker, jsonOkonomi).doMapping()

        with(jsonOkonomi.opplysninger) {
            assertThat(bostotte).isNotNull
            assertThat(bostotte.saker).hasSize(2)
                .allMatch { it.type == InntektType.UTBETALING_HUSBANKEN.toJsonInntektType() }

            bostotte.saker.find { it.status == BostotteStatus.VEDTATT.name }!!
                .let { jsonSak ->
                    bostotteSaker.map { it.dato.toString() }.let { datoer -> assertThat(datoer).contains(jsonSak.dato) }
                    assertThat(jsonSak.beskrivelse).isEqualTo("Annen beskrivelse av Bostotte")
                    assertThat(jsonSak.vedtaksstatus).isEqualTo(JsonBostotteSak.Vedtaksstatus.AVVIST)
                }
            bostotte.saker.find { it.status == BostotteStatus.UNDER_BEHANDLING.name }!!
                .let { jsonSak ->
                    bostotteSaker.map { it.dato.toString() }.let { datoer -> assertThat(datoer).contains(jsonSak.dato) }
                    assertThat(jsonSak.beskrivelse).isEqualTo("Beskrivelse av bostotte")
                    assertThat(jsonSak.vedtaksstatus).isNull()
                }
        }
    }
}

private fun InntektType.toJsonInntektType(): String {
    return OpplysningTypeMapper.getJsonVerdier(this).navn?.verdi ?: error("Finner ikke InntektType")
}
