package no.nav.sosialhjelp.soknad.v2.json.generate.mappers

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonFiler
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import no.nav.sosialhjelp.soknad.metrics.Vedleggstatus
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
        val dokList =
            listOf(
                opprettDokumentasjon(soknadId = UUID.randomUUID(), type = UtgiftType.UTGIFTER_STROM, status = DokumentasjonStatus.FORVENTET),
            )
        val uploadVedlegg = JsonVedleggSpesifikasjon(emptyList())

        val mappedJson = DokumentasjonToJsonMapper.doMapping(dokList, uploadVedlegg, json)

        val vedlegg = requireNotNull(mappedJson.vedlegg).vedlegg.orEmpty()
        assertThat(vedlegg).hasSize(1)
        assertThat(vedlegg[0].type).isEqualTo(UtgiftType.UTGIFTER_STROM.getVedleggTypeString())
        assertThat(vedlegg[0].status).isEqualTo(DokumentasjonStatus.FORVENTET.toVedleggStatusString())
        assertThat(vedlegg[0].filer).isEmpty()
    }

    @Test
    fun `Dokumentasjon med match i upload-respons bruker hele JsonVedlegg fra upload`() {
        val type = UtgiftType.UTGIFTER_STROM
        val tilleggsinfo = type.getVedleggTillegginfoString()
        val uploadFil = JsonFiler("kvittering.pdf")
        val uploadVedleggItem =
            JsonVedlegg(type.getVedleggTypeString(), tilleggsinfo, status = Vedleggstatus.LastetOpp.name, filer = listOf(uploadFil), hendelseType = JsonVedlegg.HendelseType.SOKNAD)

        val dokList =
            listOf(
                opprettDokumentasjon(soknadId = UUID.randomUUID(), type = type, status = DokumentasjonStatus.FORVENTET),
            )
        val uploadVedlegg = JsonVedleggSpesifikasjon(listOf(uploadVedleggItem))

        val mappedJson = DokumentasjonToJsonMapper.doMapping(dokList, uploadVedlegg, json)

        val vedlegg = requireNotNull(mappedJson.vedlegg).vedlegg.orEmpty()
        assertThat(vedlegg).hasSize(1)
        assertThat(vedlegg[0].type).isEqualTo(uploadVedleggItem.type)
        assertThat(vedlegg[0].status).isEqualTo(uploadVedleggItem.status)
        assertThat(vedlegg[0].filer.map { it.filnavn }).containsExactly("kvittering.pdf")
    }

    @Test
    fun `Blanding av match og ikke-match gir korrekt merge`() {
        val matchType = UtgiftType.UTGIFTER_STROM
        val noMatchType = InntektType.STUDIELAN_INNTEKT

        val uploadVedleggItem =
            JsonVedlegg(matchType.getVedleggTypeString(), matchType.getVedleggTillegginfoString(), status = Vedleggstatus.LastetOpp.name, filer = listOf(JsonFiler("fil.pdf")))

        val dokList =
            listOf(
                opprettDokumentasjon(soknadId = UUID.randomUUID(), type = matchType, status = DokumentasjonStatus.FORVENTET),
                opprettDokumentasjon(soknadId = UUID.randomUUID(), type = noMatchType, status = DokumentasjonStatus.FORVENTET),
            )
        val uploadVedlegg = JsonVedleggSpesifikasjon(listOf(uploadVedleggItem))

        val mappedJson = DokumentasjonToJsonMapper.doMapping(dokList, uploadVedlegg, json)

        val vedlegg = requireNotNull(mappedJson.vedlegg).vedlegg.orEmpty()
        assertThat(vedlegg).hasSize(2)

        val matched = vedlegg.find { it.type == matchType.getVedleggTypeString() }!!
        assertThat(matched.status).isEqualTo(Vedleggstatus.LastetOpp.name)
        assertThat(matched.filer.map { it.filnavn }).contains("fil.pdf")

        val unmatched = vedlegg.find { it.type == noMatchType.getVedleggTypeString() }!!
        assertThat(unmatched.filer).isEmpty()
        assertThat(unmatched.status).isEqualTo(DokumentasjonStatus.FORVENTET.toVedleggStatusString())
    }

    @Test
    fun `Ekstra vedlegg fra upload som ikke finnes lokalt inkluderes i resultatet`() {
        val extraType = UtgiftType.UTGIFTER_STROM
        val extraVedlegg =
            JsonVedlegg(extraType.getVedleggTypeString(), extraType.getVedleggTillegginfoString(), status = Vedleggstatus.LastetOpp.name)

        val uploadVedlegg = JsonVedleggSpesifikasjon(listOf(extraVedlegg))

        val mappedJson = DokumentasjonToJsonMapper.doMapping(emptyList(), uploadVedlegg, json)

        val vedlegg = requireNotNull(mappedJson.vedlegg).vedlegg.orEmpty()
        assertThat(vedlegg).hasSize(1)
        assertThat(vedlegg[0]).isSameAs(extraVedlegg)
    }
}
