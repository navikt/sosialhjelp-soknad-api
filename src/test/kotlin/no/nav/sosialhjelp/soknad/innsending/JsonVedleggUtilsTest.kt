package no.nav.sosialhjelp.soknad.innsending

import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonFiler
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg.HendelseType
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.Vedleggstatus
import no.nav.sosialhjelp.soknad.innsending.JsonVedleggUtils.addHendelseTypeAndHendelseReferanse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class JsonVedleggUtilsTest {

    @Test
    fun doNot_addHendelseTypeAndHendelseReferanse_ifUnleashToggleIsDeactivated() {
        val jsonVedleggSpesifikasjon = createJsonVedleggSpesifikasjon()
        assertThat(jsonVedleggSpesifikasjon.vedlegg[0].hendelseType).isNull()
        assertThat(jsonVedleggSpesifikasjon.vedlegg[0].hendelseReferanse).isNull()

        addHendelseTypeAndHendelseReferanse(jsonVedleggSpesifikasjon, true, false)

        assertThat(jsonVedleggSpesifikasjon.vedlegg[0].hendelseType).isNull()
        assertThat(jsonVedleggSpesifikasjon.vedlegg[0].hendelseReferanse).isNull()
        assertThat(jsonVedleggSpesifikasjon.vedlegg[1].hendelseType).isNull()
        assertThat(jsonVedleggSpesifikasjon.vedlegg[1].hendelseReferanse).isNull()
        assertThat(jsonVedleggSpesifikasjon.vedlegg[2].hendelseType).isNull()
        assertThat(jsonVedleggSpesifikasjon.vedlegg[2].hendelseReferanse).isNull()
    }

    @Test
    fun addHendelseTypeAndHendelseReferanse_forSoknad_ifUnleashToggleIsActivated() {
        val jsonVedleggSpesifikasjon = createJsonVedleggSpesifikasjon()
        assertThat(jsonVedleggSpesifikasjon.vedlegg[0].hendelseType).isNull()
        assertThat(jsonVedleggSpesifikasjon.vedlegg[0].hendelseReferanse).isNull()

        addHendelseTypeAndHendelseReferanse(jsonVedleggSpesifikasjon, true, true)

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
        val jsonVedleggSpesifikasjon = createJsonVedleggSpesifikasjon()
        addHendelseTypeAndHendelseReferanse(jsonVedleggSpesifikasjon, true, true)

        assertThat(jsonVedleggSpesifikasjon.vedlegg[0].hendelseReferanse)
            .isNotEqualTo(jsonVedleggSpesifikasjon.vedlegg[1].hendelseReferanse)
    }

    @Test
    fun addHendelseTypeAndHendelseReferanse_forEttersendelse_shouldOnlyAddHendelseTypeBrukerForAnnetAnnet() {
        val jsonVedleggSpesifikasjon = createJsonVedleggSpesifikasjon()
        assertThat(jsonVedleggSpesifikasjon.vedlegg[0].hendelseType).isNull()
        assertThat(jsonVedleggSpesifikasjon.vedlegg[0].hendelseReferanse).isNull()

        addHendelseTypeAndHendelseReferanse(jsonVedleggSpesifikasjon, false, true)

        assertThat(jsonVedleggSpesifikasjon.vedlegg[0].hendelseType).isNull()
        assertThat(jsonVedleggSpesifikasjon.vedlegg[0].hendelseReferanse).isNull()
        assertThat(jsonVedleggSpesifikasjon.vedlegg[1].hendelseType).isNull()
        assertThat(jsonVedleggSpesifikasjon.vedlegg[1].hendelseReferanse).isNull()
        assertThat(jsonVedleggSpesifikasjon.vedlegg[2].hendelseType).isEqualTo(HendelseType.BRUKER)
        assertThat(jsonVedleggSpesifikasjon.vedlegg[2].hendelseReferanse).isNull()
    }

    @Test
    fun addHendelseTypeAndHendelseReferanse_forEttersendelse_shouldNotEditHendelseReferanse() {
        val hendelseReferanse = "1234"
        val jsonVedleggSpesifikasjon = createJsonVedleggSpesifikasjon()
        jsonVedleggSpesifikasjon.vedlegg[0].hendelseType = HendelseType.SOKNAD
        jsonVedleggSpesifikasjon.vedlegg[0].hendelseReferanse = hendelseReferanse
        jsonVedleggSpesifikasjon.vedlegg[1].hendelseType = HendelseType.BRUKER

        addHendelseTypeAndHendelseReferanse(jsonVedleggSpesifikasjon, false, true)

        assertThat(jsonVedleggSpesifikasjon.vedlegg[0].hendelseType).isEqualTo(HendelseType.SOKNAD)
        assertThat(jsonVedleggSpesifikasjon.vedlegg[0].hendelseReferanse).isEqualTo(hendelseReferanse)
        assertThat(jsonVedleggSpesifikasjon.vedlegg[1].hendelseType).isEqualTo(HendelseType.BRUKER)
        assertThat(jsonVedleggSpesifikasjon.vedlegg[1].hendelseReferanse).isNull()
    }

    private fun createJsonVedleggSpesifikasjon(): JsonVedleggSpesifikasjon {
        val jsonVedlegg: MutableList<JsonVedlegg> = ArrayList()
        jsonVedlegg.add(
            JsonVedlegg()
                .withStatus(Vedleggstatus.VedleggKreves.name)
                .withType("annet")
                .withTilleggsinfo("tilleggsinfo1")
        )
        jsonVedlegg.add(
            JsonVedlegg()
                .withStatus(Vedleggstatus.LastetOpp.name)
                .withType("type1")
                .withTilleggsinfo("annet")
                .withFiler(lagJsonFiler())
        )
        jsonVedlegg.add(
            JsonVedlegg()
                .withStatus(Vedleggstatus.LastetOpp.name)
                .withType(JsonVedleggUtils.ANNET)
                .withTilleggsinfo(JsonVedleggUtils.ANNET)
                .withFiler(lagJsonFiler())
        )
        return JsonVedleggSpesifikasjon()
            .withVedlegg(jsonVedlegg)
    }

    private fun lagJsonFiler(): List<JsonFiler> {
        val filer: MutableList<JsonFiler> = ArrayList()
        filer.add(
            JsonFiler()
                .withFilnavn("filnavn")
                .withSha512("sha1")
        )
        return filer
    }
}
