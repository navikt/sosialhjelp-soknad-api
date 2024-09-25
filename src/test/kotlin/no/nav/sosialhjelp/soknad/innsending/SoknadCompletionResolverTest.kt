package no.nav.sosialhjelp.soknad.innsending

import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpObjectMapper
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpValidator
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import org.junit.jupiter.api.Test

class SoknadCompletionResolverTest {
    private val jsonSoknad: JsonInternalSoknad =
        SoknadServiceOld.createEmptyJsonInternalSoknad("01020312345", false)

    @Test
    fun `jsonInternalSoknad skal validere`() {
        JsonSosialhjelpValidator.ensureValidInternalSoknad(mapper.writeValueAsString(jsonSoknad))
    }

    @Test
    fun `Kalkulere prosent utfylt gir prosent tilbake`() {
        val percentageOfCompletion = SoknadCompletionResolver(jsonSoknad).percentageOfCompletion()

        val a = 4
    }

    companion object {
        private val mapper = JsonSosialhjelpObjectMapper.createObjectMapper()
    }
}
