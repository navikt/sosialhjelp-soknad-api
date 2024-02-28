package no.nav.sosialhjelp.soknad.v2.generate.mappers

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonIdentifikator
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonSokernavn
import no.nav.sosialhjelp.soknad.v2.createJsonInternalSoknadWithInitializedSuperObjects
import no.nav.sosialhjelp.soknad.v2.generate.mappers.domain.SoknadToJsonMapper
import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import no.nav.sosialhjelp.soknad.v2.soknad.NavEnhet
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class SoknadMapperTest {

    private val mapper = SoknadToJsonMapper.Mapper

    @Test
    fun `Soknad-data skal mappes til JsonInternalSoknad`() {
        val jsonInternalSoknad = createJsonInternalSoknadWithInitializedSuperObjects()
        val now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        val soknad = opprettSoknad(sendtInn = now)

        mapper.doMapping(soknad, jsonInternalSoknad)

        jsonInternalSoknad.assertInnsendingstidspunkt(now)

        jsonInternalSoknad.assertNavEnhet(soknad.mottaker)
    }

    private fun JsonInternalSoknad.assertInnsendingstidspunkt(tidspunkt: LocalDateTime) {
        assertThat(soknad.innsendingstidspunkt).isEqualTo(tidspunkt.toString())
    }

    private fun JsonInternalSoknad.assertNavEnhet(navEnhet: NavEnhet) {
        assertThat(mottaker.navEnhetsnavn).isEqualTo(navEnhet.enhetsnavn)
        assertThat(mottaker.organisasjonsnummer).isEqualTo(navEnhet.orgnummer)

        assertThat(soknad.mottaker.navEnhetsnavn).isEqualTo(navEnhet.enhetsnavn)
        assertThat(soknad.mottaker.enhetsnummer).isEqualTo(navEnhet.enhetsnummer)
        assertThat(soknad.mottaker.kommunenummer).isEqualTo(navEnhet.kommunenummer)
    }
}
