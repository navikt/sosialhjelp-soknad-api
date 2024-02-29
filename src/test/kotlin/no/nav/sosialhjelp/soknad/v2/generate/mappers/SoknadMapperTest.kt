package no.nav.sosialhjelp.soknad.v2.generate.mappers

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sosialhjelp.soknad.v2.createJsonInternalSoknadWithInitializedSuperObjects
import no.nav.sosialhjelp.soknad.v2.generate.mappers.domain.SoknadToJsonMapper
import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import no.nav.sosialhjelp.soknad.v2.soknad.Begrunnelse
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
        jsonInternalSoknad.assertBegrunnerlse(soknad.begrunnelse)
    }
}

private fun JsonInternalSoknad.assertInnsendingstidspunkt(tidspunkt: LocalDateTime) {
    assertThat(soknad.innsendingstidspunkt).isEqualTo(tidspunkt.toString())
}

private fun JsonInternalSoknad.assertBegrunnerlse(begrunnelse: Begrunnelse) {
    assertThat(soknad.data.begrunnelse).isNotNull
    assertThat(soknad.data.begrunnelse.hvaSokesOm).isEqualTo(begrunnelse.hvaSokesOm)
    assertThat(soknad.data.begrunnelse.hvorforSoke).isEqualTo(begrunnelse.hvorforSoke)
}
