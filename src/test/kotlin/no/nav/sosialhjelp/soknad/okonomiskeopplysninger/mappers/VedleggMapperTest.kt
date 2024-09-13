package no.nav.sosialhjelp.soknad.okonomiskeopplysninger.mappers

import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonFiler
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.Vedleggstatus
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggStatus
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagretVedleggMetadata
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.mappers.VedleggMapper as mapper

class VedleggMapperTest {
    @Test
    fun `Mappe filer til Mellomlagrede vedlegg`() {
        mapper.mapMellomlagredeVedleggToVedleggFrontend(
            vedlegg = createJsonVedlegg(),
            jsonOkonomi = JsonOkonomi().withOpplysninger(JsonOkonomiopplysninger().withUtgift(emptyList())),
            mellomlagredeVedlegg = createValidMellomlagredeVedlegg(),
        ).also { dto ->
            assertThat(dto.type).isEqualTo(VedleggType.AnnetAnnet)
            assertThat(dto.gruppe).isEqualTo(OkonomiskGruppeMapper.getGruppe(dto.type))
            assertThat(dto.filer).hasSize(2)
            assertThat(dto.vedleggStatus).isEqualTo(VedleggStatus.LastetOpp)
        }
    }

    @Test
    fun `Vedlegg med feil status skal fjerne filer`() {
        mapper.mapMellomlagredeVedleggToVedleggFrontend(
            vedlegg = createJsonVedlegg(Vedleggstatus.VedleggAlleredeSendt, createUnknownFiler()),
            jsonOkonomi = JsonOkonomi().withOpplysninger(JsonOkonomiopplysninger().withUtgift(emptyList())),
            mellomlagredeVedlegg = createValidMellomlagredeVedlegg(),
        ).also { dto ->
            assertThat(dto.type).isEqualTo(VedleggType.AnnetAnnet)
            assertThat(dto.vedleggStatus).isEqualTo(VedleggStatus.VedleggAlleredeSendt)
            assertThat(dto.filer).isEmpty()
        }
    }

    @Test
    fun `Vedlegg med riktig status med en ikke eksisterende fil skal fjerne ugyldig fil`() {
        val filer = createValidFilerPlusOneInvalid().also { assertThat(it).hasSize(3) }

        mapper.mapMellomlagredeVedleggToVedleggFrontend(
            vedlegg = createJsonVedlegg(Vedleggstatus.LastetOpp, filer),
            jsonOkonomi = JsonOkonomi().withOpplysninger(JsonOkonomiopplysninger().withUtgift(emptyList())),
            mellomlagredeVedlegg = createValidMellomlagredeVedlegg(),
        ).also { dto ->
            assertThat(dto.type).isEqualTo(VedleggType.AnnetAnnet)
            assertThat(dto.vedleggStatus).isEqualTo(VedleggStatus.LastetOpp)
            assertThat(dto.filer).hasSize(2)
        }
    }

    @Test
    fun `Vedlegg med riktig status har ingen kjente filer`() {
        mapper.mapMellomlagredeVedleggToVedleggFrontend(
            vedlegg = createJsonVedlegg(status = Vedleggstatus.LastetOpp, filer = createUnknownFiler()),
            jsonOkonomi = JsonOkonomi().withOpplysninger(JsonOkonomiopplysninger().withUtgift(emptyList())),
            mellomlagredeVedlegg = createValidMellomlagredeVedlegg(),
        )
            .also { dto ->
                assertThat(dto.type).isEqualTo(VedleggType.AnnetAnnet)
                assertThat(dto.vedleggStatus).isEqualTo(VedleggStatus.VedleggKreves)
                assertThat(dto.filer).hasSize(0)
            }
    }
}

private fun createValidMellomlagredeVedlegg(): List<MellomlagretVedleggMetadata> {
    return listOf(
        MellomlagretVedleggMetadata("fil1.pdf", UUID.randomUUID().toString(), "sha512"),
        MellomlagretVedleggMetadata("fil2.pdf", UUID.randomUUID().toString(), "sha512"),
    )
}

private fun createJsonVedlegg(
    status: Vedleggstatus = Vedleggstatus.LastetOpp,
    filer: List<JsonFiler> = createValidFiler(),
): JsonVedlegg {
    return JsonVedlegg()
        .withType("annet")
        .withTilleggsinfo("annet")
        .withStatus(status.toString())
        .withFiler(filer)
}

private fun createValidFiler(): List<JsonFiler> {
    return listOf(
        JsonFiler().withFilnavn("fil1.pdf").withSha512("sha512"),
        JsonFiler().withFilnavn("fil2.pdf").withSha512("sha512"),
    )
}

private fun createValidFilerPlusOneInvalid(): List<JsonFiler> {
    return listOf(
        JsonFiler().withFilnavn("fil1.pdf").withSha512("sha512"),
        JsonFiler().withFilnavn("fil2.pdf").withSha512("sha512"),
        JsonFiler().withFilnavn("fil3.pdf").withSha512("sha512"),
    )
}

private fun createUnknownFiler(): List<JsonFiler> {
    return listOf(
        JsonFiler().withFilnavn("fil99.pdf").withSha512("sha512"),
        JsonFiler().withFilnavn("fil999.pdf").withSha512("sha512"),
        JsonFiler().withFilnavn("fil9999.pdf").withSha512("sha512"),
    )
}
