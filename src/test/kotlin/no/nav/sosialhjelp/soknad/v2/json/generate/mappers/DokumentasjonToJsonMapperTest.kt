package no.nav.sosialhjelp.soknad.v2.json.generate.mappers

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonFiler
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import no.nav.sosialhjelp.soknad.v2.createJsonInternalSoknadWithInitializedSuperObjects
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonStatus
import no.nav.sosialhjelp.soknad.v2.json.generate.mappers.domain.DokumentasjonToJsonMapper
import no.nav.sosialhjelp.soknad.v2.json.generate.mappers.domain.toVedleggStatusString
import no.nav.sosialhjelp.soknad.v2.json.getVedleggTillegginfoString
import no.nav.sosialhjelp.soknad.v2.json.getVedleggTypeString
import no.nav.sosialhjelp.soknad.v2.okonomi.InntektType
import no.nav.sosialhjelp.soknad.v2.okonomi.UtgiftType
import no.nav.sosialhjelp.soknad.v2.opprettDokumentasjon
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

class DokumentasjonToJsonMapperTest {
    private val json: JsonInternalSoknad = createJsonInternalSoknadWithInitializedSuperObjects()

    @Test
    fun `Dokumentasjon uten match i upload-respons mappes med status og tom filliste`() {
        val dokList = listOf(
            opprettDokumentasjon(soknadId = UUID.randomUUID(), type = UtgiftType.UTGIFTER_STROM, status = DokumentasjonStatus.FORVENTET),
        )
        val uploadVedlegg = JsonVedleggSpesifikasjon().withVedlegg(emptyList())

        DokumentasjonToJsonMapper.doMapping(dokList, uploadVedlegg, json)

        val vedlegg = json.vedlegg.vedlegg
        assertThat(vedlegg).hasSize(1)
        assertThat(vedlegg[0].type).isEqualTo(UtgiftType.UTGIFTER_STROM.getVedleggTypeString())
        assertThat(vedlegg[0].status).isEqualTo(DokumentasjonStatus.FORVENTET.toVedleggStatusString())
        assertThat(vedlegg[0].filer).isEmpty()
    }

    @Test
    fun `Dokumentasjon med match i upload-respons bruker hele JsonVedlegg fra upload`() {
        val type = UtgiftType.UTGIFTER_STROM
        val tilleggsinfo = type.getVedleggTillegginfoString()
        val uploadFil = JsonFiler().withFilnavn("kvittering.pdf")
        val uploadVedleggItem = JsonVedlegg()
            .withType(type.getVedleggTypeString())
            .withTilleggsinfo(tilleggsinfo)
            .withStatus(DokumentasjonStatus.LASTET_OPP.toVedleggStatusString())
            .withFiler(listOf(uploadFil))
            .withHendelseType(JsonVedlegg.HendelseType.SOKNAD)

        val dokList = listOf(
            opprettDokumentasjon(soknadId = UUID.randomUUID(), type = type, status = DokumentasjonStatus.LASTET_OPP),
        )
        val uploadVedlegg = JsonVedleggSpesifikasjon().withVedlegg(listOf(uploadVedleggItem))

        DokumentasjonToJsonMapper.doMapping(dokList, uploadVedlegg, json)

        val vedlegg = json.vedlegg.vedlegg
        assertThat(vedlegg).hasSize(1)
        assertThat(vedlegg[0]).isSameAs(uploadVedleggItem)
    }

    @Test
    fun `Blanding av match og ikke-match gir korrekt merge`() {
        val matchType = UtgiftType.UTGIFTER_STROM
        val noMatchType = InntektType.STUDIELAN_INNTEKT

        val uploadVedleggItem = JsonVedlegg()
            .withType(matchType.getVedleggTypeString())
            .withTilleggsinfo(matchType.getVedleggTillegginfoString())
            .withStatus(DokumentasjonStatus.LASTET_OPP.toVedleggStatusString())
            .withFiler(listOf(JsonFiler().withFilnavn("fil.pdf")))

        val dokList = listOf(
            opprettDokumentasjon(soknadId = UUID.randomUUID(), type = matchType, status = DokumentasjonStatus.LASTET_OPP),
            opprettDokumentasjon(soknadId = UUID.randomUUID(), type = noMatchType, status = DokumentasjonStatus.FORVENTET),
        )
        val uploadVedlegg = JsonVedleggSpesifikasjon().withVedlegg(listOf(uploadVedleggItem))

        DokumentasjonToJsonMapper.doMapping(dokList, uploadVedlegg, json)

        val vedlegg = json.vedlegg.vedlegg
        assertThat(vedlegg).hasSize(2)

        val matched = vedlegg.find { it.type == matchType.getVedleggTypeString() }!!
        assertThat(matched).isSameAs(uploadVedleggItem)

        val unmatched = vedlegg.find { it.type == noMatchType.getVedleggTypeString() }!!
        assertThat(unmatched.filer).isEmpty()
        assertThat(unmatched.status).isEqualTo(DokumentasjonStatus.FORVENTET.toVedleggStatusString())
    }

    @Test
    fun `Ekstra vedlegg fra upload som ikke finnes lokalt inkluderes i resultatet`() {
        val extraType = UtgiftType.UTGIFTER_STROM
        val extraVedlegg = JsonVedlegg()
            .withType(extraType.getVedleggTypeString())
            .withTilleggsinfo(extraType.getVedleggTillegginfoString())
            .withStatus(DokumentasjonStatus.LASTET_OPP.toVedleggStatusString())

        val uploadVedlegg = JsonVedleggSpesifikasjon().withVedlegg(listOf(extraVedlegg))

        DokumentasjonToJsonMapper.doMapping(emptyList(), uploadVedlegg, json)

        val vedlegg = json.vedlegg.vedlegg
        assertThat(vedlegg).hasSize(1)
        assertThat(vedlegg[0]).isSameAs(extraVedlegg)
    }
}
