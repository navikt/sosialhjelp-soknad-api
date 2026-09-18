package no.nav.sosialhjelp.soknad.innsending

import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonFiler
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg.HendelseType
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import no.nav.sosialhjelp.soknad.metrics.VedleggskravStatistikkUtil
import no.nav.sosialhjelp.soknad.metrics.Vedleggstatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

internal class JsonDokumentasjonUtilsTest {
    @Test
    fun addHendelseTypeAndHendelseReferanse_forSoknad() {
        val jsonVedleggSpesifikasjon = addHendelseTypeAndHendelseReferanse(createJsonVedleggSpesifikasjon())
        assertThat(jsonVedleggSpesifikasjon.vedlegg[0].hendelseType).isNull()
        assertThat(jsonVedleggSpesifikasjon.vedlegg[0].hendelseReferanse).isNull()

        assertThat(jsonVedleggSpesifikasjon.vedlegg[0].hendelseType).isEqualTo(HendelseType.SOKNAD)
        assertThat(jsonVedleggSpesifikasjon.vedlegg[0].hendelseReferanse).isNotNull
        assertThat(jsonVedleggSpesifikasjon.vedlegg[1].hendelseType).isEqualTo(HendelseType.SOKNAD)
        assertThat(jsonVedleggSpesifikasjon.vedlegg[1].hendelseReferanse).isNotNull
        // annet|annet -> hendelseType:bruker uten hendelseReferanse
        assertThat(jsonVedleggSpesifikasjon.vedlegg[2].hendelseType).isEqualTo(HendelseType.BRUKER)
        assertThat(jsonVedleggSpesifikasjon.vedlegg[2].hendelseReferanse).isNull()
    }

    @Test
    fun addHendelseTypeAndHendelseReferanse_shouldAddUniqueReferanse() {
        val jsonVedleggSpesifikasjon = addHendelseTypeAndHendelseReferanse(createJsonVedleggSpesifikasjon())

        assertThat(jsonVedleggSpesifikasjon.vedlegg[0].hendelseReferanse)
            .isNotEqualTo(jsonVedleggSpesifikasjon.vedlegg[1].hendelseReferanse)
    }

    private fun createJsonVedleggSpesifikasjon(): JsonVedleggSpesifikasjon {
        return JsonVedleggSpesifikasjon(
            listOf(
                JsonVedlegg(type = "annet", tilleggsinfo = "tilleggsinfo1", status = Vedleggstatus.VedleggKreves.name),
                JsonVedlegg(type = "type1", tilleggsinfo = "annet", status = Vedleggstatus.LastetOpp.name, filer = lagJsonFiler()),
                JsonVedlegg(type = VedleggskravStatistikkUtil.ANNET, tilleggsinfo = VedleggskravStatistikkUtil.ANNET, status = Vedleggstatus.LastetOpp.name, filer = lagJsonFiler()),
            ),
        )
    }

    private fun lagJsonFiler(): List<JsonFiler> {
        return listOf(JsonFiler(filnavn = "filnavn", sha512 = "sha1"))
    }
}

private fun isVedleggskravAnnet(vedlegg: JsonVedlegg) =
    VedleggskravStatistikkUtil.ANNET == vedlegg.type &&
        VedleggskravStatistikkUtil.ANNET == vedlegg.tilleggsinfo

private fun addHendelseTypeAndHendelseReferanse(jsonVedleggSpesifikasjon: JsonVedleggSpesifikasjon): JsonVedleggSpesifikasjon =
    jsonVedleggSpesifikasjon.copy(
        vedlegg =
            jsonVedleggSpesifikasjon.vedlegg.map {
                if (isVedleggskravAnnet(it)) {
                    it.copy(hendelseType = HendelseType.BRUKER)
                } else {
                    it.copy(hendelseType = HendelseType.SOKNAD, hendelseReferanse = UUID.randomUUID().toString())
                }
            },
    )
