package no.nav.sosialhjelp.soknad.v2.json.generate.mappers

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sosialhjelp.soknad.v2.createJsonInternalSoknadWithInitializedSuperObjects
import no.nav.sosialhjelp.soknad.v2.eier.Kontonummer
import no.nav.sosialhjelp.soknad.v2.json.generate.mappers.domain.EierToJsonMapper
import no.nav.sosialhjelp.soknad.v2.opprettEier
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

class EierMapperTest {
    private lateinit var json: JsonInternalSoknad

    @BeforeEach
    fun setup() {
        json = createJsonInternalSoknadWithInitializedSuperObjects()
    }

    @Test
    fun `Eier skal mappes til Json`() {
        val eier = opprettEier(UUID.randomUUID())
        json = EierToJsonMapper.doMapping(eier, json)

        with(requireNotNull(json.soknad).data.personalia) {
            assertThat(requireNotNull(this.statsborgerskap).verdi).isEqualTo(eier.statsborgerskap)
            assertThat(requireNotNull(this.nordiskBorger).verdi).isEqualTo(eier.nordiskBorger)

            with(navn) {
                assertThat(this.fornavn).isEqualTo(eier.navn.fornavn)
                assertThat(this.mellomnavn).isEqualTo(eier.navn.mellomnavn)
                assertThat(this.etternavn).isEqualTo(eier.navn.etternavn)
            }
        }
    }

    @Test
    fun `HarIkkeKonto == true gir kilde = BRUKER og verdi = null`() {
        val eier =
            opprettEier(
                soknadId = UUID.randomUUID(),
                kontonummer =
                    Kontonummer(
                        harIkkeKonto = true,
                        fraBruker = "blabla",
                    ),
            )
        json = EierToJsonMapper.doMapping(eier, json)

        with(requireNotNull(json.soknad).data.personalia) {
            assertThat(this.kontonummer.kilde).isEqualTo(JsonKilde.BRUKER)
            assertThat(this.kontonummer.harIkkeKonto).isTrue()
            assertThat(this.kontonummer.verdi).isNull()
        }
    }

    @Test
    fun `Har brukerkonto og registerkonto - bruker skal velges`() {
        val eier =
            opprettEier(
                soknadId = UUID.randomUUID(),
                kontonummer =
                    Kontonummer(
                        fraBruker = "blabla",
                        fraRegister = "tjatja",
                    ),
            )
        json = EierToJsonMapper.doMapping(eier, json)

        with(requireNotNull(json.soknad).data.personalia) {
            assertThat(this.kontonummer.kilde).isEqualTo(JsonKilde.BRUKER)
            assertThat(this.kontonummer.harIkkeKonto).isFalse()
            assertThat(this.kontonummer.verdi).isEqualTo(eier.kontonummer.fraBruker)
        }
    }

    @Test
    fun `Kun register skal gi register i json`() {
        val eier =
            opprettEier(
                soknadId = UUID.randomUUID(),
                kontonummer =
                    Kontonummer(
                        fraRegister = "blabla",
                    ),
            )
        json = EierToJsonMapper.doMapping(eier, json)

        with(requireNotNull(json.soknad).data.personalia) {
            assertThat(this.kontonummer.kilde).isEqualTo(JsonKilde.SYSTEM)
            assertThat(this.kontonummer.harIkkeKonto == null).isTrue()
            assertThat(this.kontonummer.verdi).isEqualTo(eier.kontonummer.fraRegister)
        }
    }

    @Test
    fun `Ingen verdier satt skal gi tomt json-objekt`() {
        val eier =
            opprettEier(
                soknadId = UUID.randomUUID(),
                kontonummer = Kontonummer(),
            )
        json = EierToJsonMapper.doMapping(eier, json)
        with(requireNotNull(json.soknad).data.personalia.kontonummer) {
            assertThat(kilde).isEqualTo(JsonKilde.SYSTEM)
            assertThat(harIkkeKonto == null).isTrue()
            assertThat(verdi).isNull()
        }
    }
}
