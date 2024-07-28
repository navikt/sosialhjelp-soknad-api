package no.nav.sosialhjelp.soknad.v2.integrationtest.okonomi

import no.nav.sosialhjelp.soknad.v2.okonomi.Belop
import no.nav.sosialhjelp.soknad.v2.okonomi.ForventetDokumentasjonDto
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiOgDokumentasjonInput
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiService
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiskDetaljInput
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiskeDetaljer
import no.nav.sosialhjelp.soknad.v2.okonomi.formue.Formue
import no.nav.sosialhjelp.soknad.v2.okonomi.formue.FormueType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import java.util.UUID

class OkonomiskeOpplysningerIntegrationTest : AbstractOkonomiIntegrationTest() {
    @Autowired
    private lateinit var okonomiService: OkonomiService

    @Test
    fun `Hente okonomiske opplysninger skal returnere eksisterende data`() {
        Formue(
            type = FormueType.FORMUE_BRUKSKONTO,
            formueDetaljer =
                OkonomiskeDetaljer(
                    listOf(Belop(belop = 2400.0)),
                ),
        )
            .also { formue -> okonomiService.addElementToOkonomi(soknad.id, formue) }

        doGet(
            uri = getUrl(soknad.id),
            responseBodyClass = ForventetDokumentasjonDto::class.java,
        )
            .also { dto ->
                assertThat(dto.forventetDokumentasjon).hasSize(1)
                assertThat(dto.forventetDokumentasjon.map { it.type }).containsOnly(FormueType.FORMUE_BRUKSKONTO)
            }
    }

    @Test
    fun `FormueType uten forventet dokumentasjon genererer ikke forventet dokumentasjon`() {
        Formue(
            type = FormueType.VERDI_BOLIG,
            formueDetaljer =
                OkonomiskeDetaljer(
                    listOf(Belop(belop = 2400.0)),
                ),
        )
            .let { formue -> okonomi.copy(formuer = setOf(formue)) }
            .let { okonomi -> okonomiRepository.save(okonomi) }

        doGet(
            uri = getUrl(soknad.id),
            responseBodyClass = ForventetDokumentasjonDto::class.java,
        )
            .also { dto -> assertThat(dto.forventetDokumentasjon).isEmpty() }
    }

    @Test
    fun `Oppdatere data skal lagres i databasen`() {
        Formue(type = FormueType.FORMUE_BRUKSKONTO)
            .also { formue -> okonomiService.addElementToOkonomi(soknad.id, formue) }

        okonomiRepository.findByIdOrNull(soknad.id)!!
            .also {
                assertThat(it.formuer).hasSize(1)
                assertThat(it.formuer.first().formueDetaljer.detaljer).isEmpty()
            }

        OkonomiOgDokumentasjonInput(
            type = FormueType.FORMUE_BRUKSKONTO,
            dokumentasjonLevert = true,
            detaljer =
                listOf(
                    OkonomiskDetaljInput(belop = 2400.0),
                    OkonomiskDetaljInput(belop = 3000.0),
                    OkonomiskDetaljInput(belop = 1800.0),
                ),
        )
            .also {
                doPut(
                    uri = getUrl(soknad.id),
                    requestBody = it,
                    responseBodyClass = ForventetDokumentasjonDto::class.java,
                    soknadId = soknad.id,
                )
            }

        okonomiRepository.findByIdOrNull(soknad.id)!!
            .let {
                assertThat(it.formuer).hasSize(1)
                it.formuer.first()
            }
            .let { formue ->
                assertThat(formue.formueDetaljer.detaljer).hasSize(3)
                assertThat(formue.formueDetaljer.detaljer).allMatch { it is Belop }
            }
    }

    @Test
    fun `Oppdatere OkonomiType som ikke finnes skal gi feil`() {
        val input =
            OkonomiOgDokumentasjonInput(
                type = FormueType.FORMUE_BRUKSKONTO,
                dokumentasjonLevert = true,
                detaljer =
                    listOf(
                        OkonomiskDetaljInput(belop = 2400.0),
                        OkonomiskDetaljInput(belop = 3000.0),
                        OkonomiskDetaljInput(belop = 1800.0),
                    ),
            )

        doPutExpectError(
            uri = getUrl(soknad.id),
            requestBody = input,
            httpStatus = HttpStatus.NOT_FOUND,
            soknadId = soknad.id,
        )
            .also {
                assertThat(it.id).isEqualTo(soknad.id.toString())
            }
    }

    // TODO Special cases (Annet med beskrivelse f.eks.)

    companion object {
        fun getUrl(soknadId: UUID) = "/soknad/$soknadId/okonomiskeOpplysninger"
    }
}
