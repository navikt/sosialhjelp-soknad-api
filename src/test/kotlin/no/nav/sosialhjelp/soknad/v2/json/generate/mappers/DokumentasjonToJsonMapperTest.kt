package no.nav.sosialhjelp.soknad.v2.json.generate.mappers

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sosialhjelp.soknad.v2.createJsonInternalSoknadWithInitializedSuperObjects
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentRef
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.Dokumentasjon
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonStatus
import no.nav.sosialhjelp.soknad.v2.json.generate.mappers.domain.DokumentasjonToJsonMapper
import no.nav.sosialhjelp.soknad.v2.json.generate.mappers.domain.toVedleggStatusString
import no.nav.sosialhjelp.soknad.v2.json.getVedleggTypeString
import no.nav.sosialhjelp.soknad.v2.okonomi.OpplysningType
import no.nav.sosialhjelp.soknad.v2.okonomi.InntektType
import no.nav.sosialhjelp.soknad.v2.okonomi.UtgiftType
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

    private val mapper = jacksonObjectMapper()

    @Test
    fun whatever() {
        val a: OpplysningType = InntektType.JOBB
        val writeValueAsString = mapper.writeValueAsString(a)
        val obj = mapper.readValue(writeValueAsString, OpplysningType::class.java)

        val b = 4
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
                    DokumentRef(dokumentId = UUID.randomUUID(), filnavn = "ett filnavn"),
                ),
        ),
        opprettDokumentasjon(soknadId = UUID.randomUUID(), type = InntektType.STUDIELAN_INNTEKT, dokumenter = emptySet()),
    )
}

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = EnumA::class, name = "EnumA"),
    JsonSubTypes.Type(value = EnumB::class, name = "EnumB"),
)
interface SuperType

enum class EnumA : SuperType {
    NOE,
    ANNET,
}

enum class EnumB : SuperType {
    EN,
    TO,
}
