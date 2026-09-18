package no.nav.sosialhjelp.soknad.v2.json.generate.mappers

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeidsforhold
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker
import no.nav.sosialhjelp.soknad.v2.createJsonInternalSoknadWithInitializedSuperObjects
import no.nav.sosialhjelp.soknad.v2.json.generate.mappers.domain.LivssituasjonToJsonMapper
import no.nav.sosialhjelp.soknad.v2.json.generate.mappers.domain.SituasjonsendringToJsonMapper
import no.nav.sosialhjelp.soknad.v2.livssituasjon.Arbeidsforhold
import no.nav.sosialhjelp.soknad.v2.livssituasjon.Bosituasjon
import no.nav.sosialhjelp.soknad.v2.livssituasjon.Livssituasjon
import no.nav.sosialhjelp.soknad.v2.livssituasjon.toIsoString
import no.nav.sosialhjelp.soknad.v2.opprettLivssituasjon
import no.nav.sosialhjelp.soknad.v2.opprettUtdanning
import no.nav.sosialhjelp.soknad.v2.situasjonsendring.Situasjonsendring
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

class LivssituasjonMapperTest {
    private lateinit var json: JsonInternalSoknad

    @BeforeEach
    fun setup() {
        json = createJsonInternalSoknadWithInitializedSuperObjects()
    }

    @Test
    fun `Manglende bosituasjon skal gi bosituasjon med kun kilde bruker`() {
        json = LivssituasjonToJsonMapper.doMapping(Livssituasjon(UUID.randomUUID()), json)

        val bosituasjon = requireNotNull(json.soknad?.data?.bosituasjon)
        assertThat(bosituasjon.kilde).isEqualTo(JsonKildeBruker.BRUKER)
        assertThat(bosituasjon.botype).isNull()
        assertThat(bosituasjon.antallPersoner).isNull()
    }

    @Test
    fun `Bosituasjon uten verdier skal ikke mappes`() {
        json = LivssituasjonToJsonMapper.doMapping(Livssituasjon(UUID.randomUUID(), bosituasjon = Bosituasjon()), json)

        assertThat(json.soknad?.data?.bosituasjon).isNull()
    }

    @Test
    fun `Arbeid skal mappes til Json`() {
        val livssituasjon = opprettLivssituasjon(UUID.randomUUID())
        json = LivssituasjonToJsonMapper.doMapping(livssituasjon, json)
        val arbeid = livssituasjon.arbeid

        with(requireNotNull(json.soknad).data) {
            assertThat(requireNotNull(this.arbeid?.kommentarTilArbeidsforhold).verdi).isEqualTo(arbeid.kommentar)
            requireNotNull(this.arbeid).forhold.forEachIndexed { index, json ->
                json.assertArbeidsforhold(arbeid.arbeidsforhold[index])
            }
        }
    }

    @Test
    fun `Utdanning skal mappes til Json`() {
        val livssituasjon = opprettLivssituasjon(UUID.randomUUID())
        json = LivssituasjonToJsonMapper.doMapping(livssituasjon, json)
        val domainUtdanning = requireNotNull(livssituasjon.utdanning)

        with(requireNotNull(json.soknad).data) {
            val jsonUtdanning = requireNotNull(this.utdanning)
            assertThat(jsonUtdanning.erStudent).isEqualTo(domainUtdanning.erStudent)
            assertThat(jsonUtdanning.studentgrad?.name).isEqualTo(domainUtdanning.studentgrad?.name)
        }
    }

    @Test
    fun `erStudent satt til false skal gi studentgrad = null`() {
        val utdanning = opprettUtdanning(erStudent = false)
        json = LivssituasjonToJsonMapper.doMapping(opprettLivssituasjon(UUID.randomUUID(), utdanning = utdanning), json)

        with(requireNotNull(json.soknad).data) {
            assertThat(requireNotNull(this.utdanning).erStudent).isEqualTo(utdanning.erStudent)
            assertThat(requireNotNull(this.utdanning).studentgrad).isNull()
        }
    }

    @Test
    fun `Bosituasjon skal mappes til Json`() {
        val livssituasjon = opprettLivssituasjon(UUID.randomUUID())
        json = LivssituasjonToJsonMapper.doMapping(livssituasjon, json)
        val domainBosituasjon = requireNotNull(livssituasjon.bosituasjon)

        with(requireNotNull(json.soknad).data) {
            val jsonBosituasjon = requireNotNull(this.bosituasjon)
            assertThat(jsonBosituasjon.botype?.name).isEqualTo(domainBosituasjon.botype?.name)
            assertThat(jsonBosituasjon.antallPersoner).isEqualTo(domainBosituasjon.antallHusstand)
        }
    }

    @Test
    fun `Situasjonsendring skal mappes til Json`() {
        val situasjonsendring = Situasjonsendring(UUID.randomUUID(), "Noe er endret", true)
        json = SituasjonsendringToJsonMapper.doMapping(situasjonsendring, json)
        with(requireNotNull(json.soknad?.data?.situasjonendring)) {
            assertThat(harNoeEndretSeg).isTrue()
            assertThat(hvaHarEndretSeg).isEqualTo("Noe er endret")
            assertThat(kilde).isEqualTo(JsonKildeBruker.BRUKER)
        }
    }
}

private fun JsonArbeidsforhold.assertArbeidsforhold(arbeidsforhold: Arbeidsforhold) {
    assertThat(kilde).isEqualTo(JsonKilde.SYSTEM)
    assertThat(arbeidsgivernavn).isEqualTo(arbeidsforhold.arbeidsgivernavn)
    assertThat(fom).isEqualTo(arbeidsforhold.start?.toIsoString())
    assertThat(tom).isEqualTo(arbeidsforhold.slutt?.toIsoString())
    assertThat(stillingsprosent).isEqualTo(arbeidsforhold.fastStillingsprosent?.toInt())
    when (arbeidsforhold.harFastStilling) {
        null -> assertThat(stillingstype).isNull()
        true -> assertThat(stillingstype).isEqualTo(JsonArbeidsforhold.Stillingstype.FAST)
        else -> assertThat(stillingstype).isEqualTo(JsonArbeidsforhold.Stillingstype.VARIABEL)
    }
}
