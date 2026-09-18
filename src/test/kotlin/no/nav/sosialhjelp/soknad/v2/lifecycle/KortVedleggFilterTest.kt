package no.nav.sosialhjelp.soknad.v2.lifecycle

import no.nav.sbl.soknadsosialhjelp.soknad.JsonData
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonFiler
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import no.nav.sosialhjelp.soknad.v2.createValidEmptyJsonInternalSoknad
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class KortVedleggFilterTest {
    private val medFil = JsonVedlegg(type = "kort", tilleggsinfo = "behov", status = "LastetOpp", filer = listOf(JsonFiler("fil.pdf")))
    private val utenFil = JsonVedlegg(type = "annet", tilleggsinfo = "annet", status = "VedleggKreves")

    @Test
    fun `Kort soknad fjerner vedlegg uten filer`() {
        val json = soknad(JsonData.Soknadstype.KORT).withoutVedleggUtenFilerForKort()

        assertThat(requireNotNull(json.vedlegg).vedlegg).containsExactly(medFil)
    }

    @Test
    fun `Standard soknad beholder alle vedlegg`() {
        val json = soknad(JsonData.Soknadstype.STANDARD).withoutVedleggUtenFilerForKort()

        assertThat(requireNotNull(json.vedlegg).vedlegg).containsExactly(medFil, utenFil)
    }

    private fun soknad(type: JsonData.Soknadstype): JsonInternalSoknad {
        val base = createValidEmptyJsonInternalSoknad()
        val soknad = requireNotNull(base.soknad)
        return base.copy(
            soknad = soknad.copy(data = soknad.data.copy(soknadstype = type)),
            vedlegg = JsonVedleggSpesifikasjon(listOf(medFil, utenFil)),
        )
    }
}
