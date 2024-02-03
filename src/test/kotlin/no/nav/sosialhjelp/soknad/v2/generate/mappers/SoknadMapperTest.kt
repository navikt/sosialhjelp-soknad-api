package no.nav.sosialhjelp.soknad.v2.generate.mappers

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonIdentifikator
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonSokernavn
import no.nav.sosialhjelp.soknad.v2.createJsonInternalSoknadWithInitializedSuperObjects
import no.nav.sosialhjelp.soknad.v2.createSoknad
import no.nav.sosialhjelp.soknad.v2.generate.mappers.domain.SoknadToJsonMapper
import no.nav.sosialhjelp.soknad.v2.soknad.Eier
import no.nav.sosialhjelp.soknad.v2.soknad.Navn
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class SoknadMapperTest {

    private val mapper = SoknadToJsonMapper.SoknadMapper

    @Test
    fun `Soknad-data skal mappes til JsonInternalSoknad`() {
        val jsonInternalSoknad = createJsonInternalSoknadWithInitializedSuperObjects()
        val now = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
        val soknad = createSoknad(innsendingstidspunkt = now)

        mapper.doMapping(soknad, jsonInternalSoknad)

        jsonInternalSoknad.assertInnsendingstidspunkt(now)

        jsonInternalSoknad.soknad.data.personalia
            .run {
                assertKilder()
                assertEier(soknad.eier)
                assertNavn(soknad.eier.navn)
            }
    }

    private fun JsonInternalSoknad.assertInnsendingstidspunkt(tidspunkt: LocalDateTime) {
        assertThat(soknad.innsendingstidspunkt).isEqualTo(tidspunkt.toString())
    }

    private fun JsonPersonalia.assertKilder() {
        assertThat(personIdentifikator.kilde).isEqualTo(JsonPersonIdentifikator.Kilde.SYSTEM)
        assertThat(navn.kilde).isEqualTo(JsonSokernavn.Kilde.SYSTEM)
        assertThat(statsborgerskap.kilde).isEqualTo(JsonKilde.SYSTEM)
        assertThat(kontonummer.kilde).isEqualTo(JsonKilde.SYSTEM)
        assertThat(nordiskBorger.kilde).isEqualTo(JsonKilde.SYSTEM)
        assertThat(telefonnummer.kilde).isEqualTo(JsonKilde.SYSTEM)
    }

    private fun JsonPersonalia.assertEier(eier: Eier) {
        assertThat(personIdentifikator.verdi).isEqualTo(eier.personId)
        assertThat(statsborgerskap.verdi).isEqualTo(eier.statsborgerskap)
        assertThat(nordiskBorger.verdi).isEqualTo(eier.nordiskBorger)
        assertThat(kontonummer.verdi).isEqualTo(eier.kontonummer)
        assertThat(telefonnummer.verdi).isEqualTo(eier.telefonnummer)
    }

    private fun JsonPersonalia.assertNavn(navnEier: Navn) {
        assertThat(navn.fornavn).isEqualTo(navnEier.fornavn)
        assertThat(navn.mellomnavn).isEqualTo(navnEier.mellomnavn)
        assertThat(navn.etternavn).isEqualTo(navnEier.etternavn)
    }
}
