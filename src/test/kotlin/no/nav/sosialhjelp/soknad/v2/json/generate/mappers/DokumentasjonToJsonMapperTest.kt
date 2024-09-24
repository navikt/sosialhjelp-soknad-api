package no.nav.sosialhjelp.soknad.v2.json.generate.mappers

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sosialhjelp.soknad.v2.createJsonInternalSoknadWithInitializedSuperObjects
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.Dokument
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.Dokumentasjon
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonStatus
import no.nav.sosialhjelp.soknad.v2.json.generate.mappers.domain.DokumentasjonToJsonMapper
import no.nav.sosialhjelp.soknad.v2.json.generate.mappers.domain.toVedleggStatusString
import no.nav.sosialhjelp.soknad.v2.json.getVedleggTypeString
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.InntektType
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.UtgiftType
import no.nav.sosialhjelp.soknad.v2.opprettDokumentasjon
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

class DokumentasjonToJsonMapperTest {
    private val json: JsonInternalSoknad = createJsonInternalSoknadWithInitializedSuperObjects()

    @Test
    fun `Dokumentasjon skal mappes til JsonVedlegg`() {
        val dokList = createDokumentasjonList()

        DokumentasjonToJsonMapper.doMapping(dokList, json)

        json.vedlegg.also { jsonVedleggSpek ->
            assertThat(jsonVedleggSpek.vedlegg).hasSize(3)

            dokList.find { it.type == UtgiftType.UTGIFTER_ANDRE_UTGIFTER }!!.let { dokumentasjon ->
                assertThat(jsonVedleggSpek.vedlegg).anyMatch {
                    it.type == dokumentasjon.type.getVedleggTypeString() &&
                        it.status == dokumentasjon.status.toVedleggStatusString() &&
                        dokumentasjon.dokumenter.size == it.filer.size
                }
            }

            dokList.find { it.type == UtgiftType.UTGIFTER_STROM }!!.let { dokumentasjon ->
                assertThat(jsonVedleggSpek.vedlegg).anyMatch {
                    it.type == dokumentasjon.type.getVedleggTypeString() &&
                        it.status == dokumentasjon.status.toVedleggStatusString() &&
                        dokumentasjon.dokumenter.size == it.filer.size
                }
            }

            dokList.find { it.type == InntektType.STUDIELAN_INNTEKT }!!.let { dokumentasjon ->
                assertThat(jsonVedleggSpek.vedlegg).anyMatch {
                    it.type == dokumentasjon.type.getVedleggTypeString() &&
                        it.status == dokumentasjon.status.toVedleggStatusString() &&
                        dokumentasjon.dokumenter.size == it.filer.size
                }
            }
        }
    }
}

private fun createDokumentasjonList(): List<Dokumentasjon> {
    return listOf(
        opprettDokumentasjon(soknadId = UUID.randomUUID(), status = DokumentasjonStatus.FORVENTET, dokumenter = emptySet()),
        opprettDokumentasjon(
            soknadId = UUID.randomUUID(),
            type = UtgiftType.UTGIFTER_ANDRE_UTGIFTER,
            dokumenter =
                setOf(
                    Dokument(dokumentId = UUID.randomUUID(), filnavn = "ett filnavn", sha512 = "sha512"),
                ),
        ),
        opprettDokumentasjon(soknadId = UUID.randomUUID(), type = InntektType.STUDIELAN_INNTEKT, dokumenter = emptySet()),
    )
}
