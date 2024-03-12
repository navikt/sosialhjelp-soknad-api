package no.nav.sosialhjelp.soknad.v2.json.generate.mappers

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sosialhjelp.soknad.v2.createJsonInternalSoknadWithInitializedSuperObjects
import no.nav.sosialhjelp.soknad.v2.json.generate.mappers.domain.SoknadToJsonMapper
import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import no.nav.sosialhjelp.soknad.v2.soknad.Begrunnelse
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class SoknadMapperTest {

    private val mapper = SoknadToJsonMapper

    @Test
    fun `Soknad-data skal mappes til JsonInternalSoknad`() {
        val jsonInternalSoknad = createJsonInternalSoknadWithInitializedSuperObjects()
        val now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        val soknad = opprettSoknad(sendtInn = now)

        SoknadToJsonMapper.doMapping(soknad, jsonInternalSoknad)

        jsonInternalSoknad.assertInnsendingstidspunkt(now)
        jsonInternalSoknad.assertBegrunnelse(soknad.begrunnelse)
    }
}

private fun JsonInternalSoknad.assertInnsendingstidspunkt(tidspunkt: LocalDateTime) {
    Assertions.assertThat(soknad.innsendingstidspunkt).isEqualTo(tidspunkt.toString())
}

private fun JsonInternalSoknad.assertBegrunnelse(begrunnelse: Begrunnelse?) {
    begrunnelse?.let {
        Assertions.assertThat(soknad.data.begrunnelse).isNotNull
        Assertions.assertThat(soknad.data.begrunnelse.hvaSokesOm).isEqualTo(it.hvaSokesOm)
        Assertions.assertThat(soknad.data.begrunnelse.hvorforSoke).isEqualTo(it.hvorforSoke)
    }
        ?: Assertions.assertThat(soknad.data.begrunnelse).isNull()
}
