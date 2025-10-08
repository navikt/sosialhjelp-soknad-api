package no.nav.sosialhjelp.soknad.v2.json.generate.mappers

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sosialhjelp.soknad.v2.createJsonInternalSoknadWithInitializedSuperObjects
import no.nav.sosialhjelp.soknad.v2.json.generate.TimestampUtil
import no.nav.sosialhjelp.soknad.v2.json.generate.TimestampUtil.nowWithMillis
import no.nav.sosialhjelp.soknad.v2.json.generate.mappers.domain.SoknadToJsonMapper
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.v2.metadata.Tidspunkt
import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import no.nav.sosialhjelp.soknad.v2.soknad.Begrunnelse
import no.nav.sosialhjelp.soknad.v2.soknad.Kategori
import no.nav.sosialhjelp.soknad.v2.soknad.Kategorier
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class SoknadToJsonMapperTest {
    @Test
    fun `Soknad-data skal mappes til JsonInternalSoknad`() {
        val jsonInternalSoknad = createJsonInternalSoknadWithInitializedSuperObjects()
        val now = nowWithMillis()
        val soknad = opprettSoknad()
        val metadata = SoknadMetadata(soknad.id, "1234561212345", tidspunkt = Tidspunkt(sendtInn = now))

        SoknadToJsonMapper.doMapping(soknad, metadata, jsonInternalSoknad)

        jsonInternalSoknad.assertInnsendingstidspunkt(now)
        jsonInternalSoknad.assertBegrunnelse(soknad.begrunnelse)
    }

    @Test
    fun `Begrunnelse med kategori skal mappes til JsonInternalSoknad`() {
        val jsonInternalSoknad = createJsonInternalSoknadWithInitializedSuperObjects()
        val now = nowWithMillis()
        val annet = "Trenger penger til bil"
        val soknad =
            opprettSoknad(
                begrunnelse =
                    createKategorier(
                        annet = annet,
                        Kategori.HUSLEIE,
                        Kategori.NODHJELP_IKKE_BOSTED,
                    ),
            )

        val metadata = SoknadMetadata(soknad.id, "1234561212345", tidspunkt = Tidspunkt(sendtInn = now))

        SoknadToJsonMapper.doMapping(soknad, metadata, jsonInternalSoknad)

        jsonInternalSoknad.assertInnsendingstidspunkt(now)
        jsonInternalSoknad.assertKategorier(soknad.begrunnelse.kategorier)
    }
}

private fun JsonInternalSoknad.assertInnsendingstidspunkt(tidspunkt: LocalDateTime) {
    TimestampUtil.convertToOffsettDateTimeUTCString(tidspunkt).also {
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

// TODO Denne asserter ingen kategorier?
private fun JsonInternalSoknad.assertKategorier(kategorier: Kategorier) {
    assertThat(soknad.data.begrunnelse).isNotNull
    assertThat(soknad.data.begrunnelse.hvaSokesOm).isNotNull()
}

private fun createKategorier(
    annet: String,
    vararg kategorier: Kategori,
): Begrunnelse {
    return Begrunnelse(kategorier = Kategorier(definerte = kategorier.toSet(), annet = annet))
}
