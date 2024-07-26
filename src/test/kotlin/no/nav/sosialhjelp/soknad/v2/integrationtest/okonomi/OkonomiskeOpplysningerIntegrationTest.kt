package no.nav.sosialhjelp.soknad.v2.integrationtest.okonomi

import no.nav.sosialhjelp.soknad.v2.okonomi.Belop
import no.nav.sosialhjelp.soknad.v2.okonomi.ForventetDokumentasjonDto
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiService
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiskeDetaljer
import no.nav.sosialhjelp.soknad.v2.okonomi.formue.Formue
import no.nav.sosialhjelp.soknad.v2.okonomi.formue.FormueType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
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
    fun `FormueType uten forventet dokumentasjon skal returnere tom liste`() {
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

    companion object {
        fun getUrl(soknadId: UUID) = "/soknad/$soknadId/okonomiskeOpplysninger"
    }
}
