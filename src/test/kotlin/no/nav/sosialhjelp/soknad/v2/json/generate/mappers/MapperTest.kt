package no.nav.sosialhjelp.soknad.v2.json.generate.mappers

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeidsforhold
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker
import no.nav.sosialhjelp.soknad.v2.createJsonInternalSoknadWithInitializedSuperObjects
import no.nav.sosialhjelp.soknad.v2.json.generate.mappers.domain.LivssituasjonToJsonMapper
import no.nav.sosialhjelp.soknad.v2.json.generate.mappers.domain.SituasjonsendringToJsonMapper
import no.nav.sosialhjelp.soknad.v2.livssituasjon.Arbeidsforhold
import no.nav.sosialhjelp.soknad.v2.opprettLivssituasjon
import no.nav.sosialhjelp.soknad.v2.opprettUtdanning
import no.nav.sosialhjelp.soknad.v2.situasjonsendring.Situasjonsendring
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

class MapperTest {
    private lateinit var json: JsonInternalSoknad

    @BeforeEach
    fun setup() {
        json = createJsonInternalSoknadWithInitializedSuperObjects()
    }

    @Test
    fun `Arbeid skal mappes til Json`() {
        val arbeid =
            opprettLivssituasjon(UUID.randomUUID())
                .also { LivssituasjonToJsonMapper.doMapping(it, json) }
                .arbeid

        with(json.soknad.data) {
            assertThat(this.arbeid.kommentarTilArbeidsforhold.verdi).isEqualTo(arbeid.kommentar)
            this.arbeid.forhold.forEachIndexed { index, json ->
                json.assertArbeidsforhold(arbeid.arbeidsforhold[index])
            }
        }
    }

    @Test
    fun `Utdanning skal mappes til Json`() {
        val utdanning =
            opprettLivssituasjon(UUID.randomUUID())
                .also { LivssituasjonToJsonMapper.doMapping(it, json) }
                .utdanning

        with(json.soknad.data) {
            assertThat(this.utdanning.erStudent).isEqualTo(utdanning!!.erStudent)
            assertThat(this.utdanning.studentgrad.name).isEqualTo(utdanning.studentgrad?.name)
        }
    }

    @Test
    fun `erStudent satt til false skal gi studentgrad = null`() {
        val utdanning =
            opprettLivssituasjon(
                soknadId = UUID.randomUUID(),
                utdanning = opprettUtdanning(erStudent = false),
            ).also { LivssituasjonToJsonMapper.doMapping(it, json) }
                .utdanning

        with(json.soknad.data) {
            assertThat(this.utdanning.erStudent).isEqualTo(utdanning!!.erStudent)
            assertThat(this.utdanning.studentgrad).isNull()
        }
    }

    @Test
    fun `Bosituasjon skal mappes til Json`() {
        val bosituasjon =
            opprettLivssituasjon(UUID.randomUUID())
                .also { LivssituasjonToJsonMapper.doMapping(it, json) }
                .bosituasjon

        with(json.soknad.data) {
            assertThat(this.bosituasjon.botype.name).isEqualTo(bosituasjon!!.botype?.name)
            assertThat(this.bosituasjon.antallPersoner).isEqualTo(bosituasjon.antallHusstand)
        }
    }

    @Test
    fun `Situasjonsendring skal mappes til Json`() {
        val situasjonsendring = Situasjonsendring(UUID.randomUUID(), "Noe er endret", true)
        SituasjonsendringToJsonMapper.doMapping(situasjonsendring, json)
        with(json.soknad.data.situasjonendring) {
            assertThat(harNoeEndretSeg).isTrue()
            assertThat(hvaHarEndretSeg).isEqualTo("Noe er endret")
            assertThat(kilde).isEqualTo(JsonKildeBruker.BRUKER)
        }
    }
}

private fun JsonArbeidsforhold.assertArbeidsforhold(arbeidsforhold: Arbeidsforhold) {
    assertThat(kilde).isEqualTo(JsonKilde.SYSTEM)
    assertThat(arbeidsgivernavn).isEqualTo(arbeidsforhold.arbeidsgivernavn)
    assertThat(fom).isEqualTo(arbeidsforhold.start)
    assertThat(tom).isEqualTo(arbeidsforhold.slutt)
    assertThat(stillingsprosent).isEqualTo(arbeidsforhold.fastStillingsprosent)
    when (arbeidsforhold.harFastStilling) {
        null -> assertThat(stillingstype).isNull()
        true -> assertThat(stillingstype).isEqualTo(JsonArbeidsforhold.Stillingstype.FAST)
        else -> assertThat(stillingstype).isEqualTo(JsonArbeidsforhold.Stillingstype.VARIABEL)
    }
}
