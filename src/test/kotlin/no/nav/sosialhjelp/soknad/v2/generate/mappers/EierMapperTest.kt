package no.nav.sosialhjelp.soknad.v2.generate.mappers

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sosialhjelp.soknad.v2.createJsonInternalSoknadWithInitializedSuperObjects
import no.nav.sosialhjelp.soknad.v2.eier.Kontonummer
import no.nav.sosialhjelp.soknad.v2.generate.mappers.domain.EierToJsonMapper
import no.nav.sosialhjelp.soknad.v2.opprettEier
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class EierMapperTest {

    private val mapper = EierToJsonMapper.Mapper
    private lateinit var json: JsonInternalSoknad

    @BeforeEach
    fun setup() {
        json = createJsonInternalSoknadWithInitializedSuperObjects()
    }

    @Test
    fun `Eier skal mappes til Json`() {
        val eier = opprettEier(UUID.randomUUID()).also { mapper.doMapping(it, json) }

        with(json.soknad.data.personalia) {
            assertThat(this.statsborgerskap.verdi).isEqualTo(eier.statsborgerskap)
            assertThat(this.nordiskBorger.verdi).isEqualTo(eier.nordiskBorger)

            with(navn) {
                assertThat(this.fornavn).isEqualTo(eier.navn.fornavn)
                assertThat(this.mellomnavn).isEqualTo(eier.navn.mellomnavn)
                assertThat(this.etternavn).isEqualTo(eier.navn.etternavn)
            }
        }
    }

    @Test
    fun `HarIkkeKonto == true gir kilde = BRUKER og verdi = null`() {
        val eier = opprettEier(
            soknadId = UUID.randomUUID(),
            kontonummer = Kontonummer(
                harIkkeKonto = true,
                bruker = "blabla"
            )
        )
        mapper.doMapping(eier, json)

        with(json.soknad.data.personalia) {
            assertThat(this.kontonummer.kilde).isEqualTo(JsonKilde.BRUKER)
            assertThat(this.kontonummer.harIkkeKonto).isTrue()
            assertThat(this.kontonummer.verdi).isNull()
        }
    }

    @Test
    fun `Har brukerkonto og registerkonto - bruker skal velges`() {
        val eier = opprettEier(
            soknadId = UUID.randomUUID(),
            kontonummer = Kontonummer(
                bruker = "blabla",
                register = "tjatja"
            )
        )
        mapper.doMapping(eier, json)

        with(json.soknad.data.personalia) {
            assertThat(this.kontonummer.kilde).isEqualTo(JsonKilde.BRUKER)
            assertThat(this.kontonummer.harIkkeKonto == null).isTrue()
            assertThat(this.kontonummer.verdi).isEqualTo(eier.kontonummer.bruker)
        }
    }

    @Test
    fun `Kun register skal gi register i json`() {
        val eier = opprettEier(
            soknadId = UUID.randomUUID(),
            kontonummer = Kontonummer(
                register = "blabla"
            )
        )
        mapper.doMapping(eier, json)

        with(json.soknad.data.personalia) {
            assertThat(this.kontonummer.kilde).isEqualTo(JsonKilde.SYSTEM)
            assertThat(this.kontonummer.harIkkeKonto == null).isTrue()
            assertThat(this.kontonummer.verdi).isEqualTo(eier.kontonummer.register)
        }
    }

    @Test
    fun `Ingen verdier satt skal gi json == null`() {
        val eier = opprettEier(
            soknadId = UUID.randomUUID(),
            kontonummer = Kontonummer()
        )
        mapper.doMapping(eier, json)
        assertThat(json.soknad.data.personalia.kontonummer).isNull()
    }
}
