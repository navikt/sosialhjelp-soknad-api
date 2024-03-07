package no.nav.sosialhjelp.soknad.v2.json.generate.mappers

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeidsforhold
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sosialhjelp.soknad.v2.createJsonInternalSoknadWithInitializedSuperObjects
import no.nav.sosialhjelp.soknad.v2.json.generate.mappers.domain.LivssituasjonToJsonMapper
import no.nav.sosialhjelp.soknad.v2.livssituasjon.Arbeidsforhold
import no.nav.sosialhjelp.soknad.v2.opprettLivssituasjon
import no.nav.sosialhjelp.soknad.v2.opprettUtdanning
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class MapperTest {

    private val mapper = LivssituasjonToJsonMapper
    private lateinit var json: JsonInternalSoknad

    @BeforeEach
    fun setup() {
        json = createJsonInternalSoknadWithInitializedSuperObjects()
    }

    @Test
    fun `Arbeid skal mappes til Json`() {
        val arbeid = opprettLivssituasjon(UUID.randomUUID())
            .also { LivssituasjonToJsonMapper.doMapping(it, json) }
            .arbeid

        with(json.soknad.data) {
            Assertions.assertThat(this.arbeid.kommentarTilArbeidsforhold.verdi).isEqualTo(arbeid.kommentar)
            this.arbeid.forhold.forEachIndexed { index, json ->
                json.assertArbeidsforhold(arbeid.arbeidsforhold[index])
            }
        }
    }

    @Test
    fun `Utdanning skal mappes til Json`() {
        val utdanning = opprettLivssituasjon(UUID.randomUUID())
            .also { LivssituasjonToJsonMapper.doMapping(it, json) }
            .utdanning

        with(json.soknad.data) {
            Assertions.assertThat(this.utdanning.erStudent).isEqualTo(utdanning.erStudent)
            Assertions.assertThat(this.utdanning.studentgrad.name).isEqualTo(utdanning.studentgrad?.name)
        }
    }

    @Test
    fun `erStudent satt til false skal gi studentgrad = null`() {
        val utdanning = opprettLivssituasjon(
            soknadId = UUID.randomUUID(),
            utdanning = opprettUtdanning(erStudent = false)
        )
            .also { LivssituasjonToJsonMapper.doMapping(it, json) }
            .utdanning

        with(json.soknad.data) {
            Assertions.assertThat(this.utdanning.erStudent).isEqualTo(utdanning.erStudent)
            Assertions.assertThat(this.utdanning.studentgrad).isNull()
        }
    }

    @Test
    fun `Bosituasjon skal mappes til Json`() {
        val bosituasjon = opprettLivssituasjon(UUID.randomUUID())
            .also { LivssituasjonToJsonMapper.doMapping(it, json) }
            .bosituasjon

        with(json.soknad.data) {
            Assertions.assertThat(this.bosituasjon.botype.name).isEqualTo(bosituasjon.botype?.name)
            Assertions.assertThat(this.bosituasjon.antallPersoner).isEqualTo(bosituasjon.antallHusstand)
        }
    }
}

private fun JsonArbeidsforhold.assertArbeidsforhold(arbeidsforhold: Arbeidsforhold) {
    Assertions.assertThat(kilde).isEqualTo(JsonKilde.SYSTEM)
    Assertions.assertThat(arbeidsgivernavn).isEqualTo(arbeidsforhold.arbeidsgivernavn)
    Assertions.assertThat(fom).isEqualTo(arbeidsforhold.start)
    Assertions.assertThat(tom).isEqualTo(arbeidsforhold.slutt)
    Assertions.assertThat(stillingsprosent).isEqualTo(arbeidsforhold.fastStillingsprosent)
    when (arbeidsforhold.harFastStilling) {
        null -> Assertions.assertThat(stillingstype).isNull()
        true -> Assertions.assertThat(stillingstype).isEqualTo(JsonArbeidsforhold.Stillingstype.FAST)
        else -> Assertions.assertThat(stillingstype).isEqualTo(JsonArbeidsforhold.Stillingstype.VARIABEL)
    }
}