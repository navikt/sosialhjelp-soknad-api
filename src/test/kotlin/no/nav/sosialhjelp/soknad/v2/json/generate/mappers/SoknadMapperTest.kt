package no.nav.sosialhjelp.soknad.v2.json.generate.mappers

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sosialhjelp.soknad.nowWithMillis
import no.nav.sosialhjelp.soknad.v2.createJsonInternalSoknadWithInitializedSuperObjects
import no.nav.sosialhjelp.soknad.v2.json.generate.TimestampConverter
import no.nav.sosialhjelp.soknad.v2.json.generate.mappers.domain.SoknadToJsonMapper
import no.nav.sosialhjelp.soknad.v2.metadata.Tidspunkt
import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import no.nav.sosialhjelp.soknad.v2.soknad.Begrunnelse
import no.nav.sosialhjelp.soknad.v2.soknad.Kategori
import no.nav.sosialhjelp.soknad.v2.soknad.Kategorier
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class SoknadMapperTest {
    @Test
    fun `Soknad-data skal mappes til JsonInternalSoknad`() {
        val jsonInternalSoknad = createJsonInternalSoknadWithInitializedSuperObjects()
        val now = nowWithMillis()
        val soknad = opprettSoknad()
        val tidspunkt = Tidspunkt(sendtInn = now)

        SoknadToJsonMapper.doMapping(soknad, tidspunkt, jsonInternalSoknad)

        jsonInternalSoknad.assertInnsendingstidspunkt(now)
        jsonInternalSoknad.assertBegrunnelse(soknad.begrunnelse)
    }

    @Test
    fun `Begrunnelse med kategori skal mappes til JsonInternalSoknad`() {
        val jsonInternalSoknad = createJsonInternalSoknadWithInitializedSuperObjects()
        val now = nowWithMillis()
        val soknad = opprettSoknad(begrunnelse = createKategorier(Kategori.Husleie, Kategori.Nodhjelp.IkkeBosted))
        val tidspunkt = Tidspunkt(sendtInn = now)

        SoknadToJsonMapper.doMapping(soknad, tidspunkt, jsonInternalSoknad)

        jsonInternalSoknad.assertInnsendingstidspunkt(now)
        jsonInternalSoknad.assertKategorier(soknad.begrunnelse.kategorier)
    }
}

private fun JsonInternalSoknad.assertInnsendingstidspunkt(tidspunkt: LocalDateTime) {
    TimestampConverter.convertToOffsettDateTimeUTCString(tidspunkt).also {
        assertThat(soknad.innsendingstidspunkt).isEqualTo(it)
    }
}

private fun JsonInternalSoknad.assertBegrunnelse(begrunnelse: Begrunnelse?) {
    begrunnelse?.let {
        assertThat(soknad.data.begrunnelse).isNotNull
        assertThat(soknad.data.begrunnelse.hvaSokesOm).isEqualTo(it.hvaSokesOm)
        assertThat(soknad.data.begrunnelse.hvorforSoke).isEqualTo(it.hvorforSoke)
    }
        ?: assertThat(soknad.data.begrunnelse).isNull()
}

private fun JsonInternalSoknad.assertKategorier(kategorier: Kategorier) {
    assertThat(soknad.data.begrunnelse).isNotNull
    assertThat(soknad.data.begrunnelse.hvaSokesOm).isNotNull()
    soknad.data.begrunnelse.hvaSokesOm.also { hvaSokesOm ->
        kategorier.sett.forEach { kategori -> assertThat(hvaSokesOm).contains(kategori.key) }
    }
}

private fun createKategorier(vararg kategorier: Kategori): Begrunnelse {
    return Begrunnelse(kategorier = Kategorier(sett = kategorier.toSet()))
}
