package no.nav.sosialhjelp.soknad.v2.integrationtest

import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonInput
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonService
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonStatus.LEVERT_TIDLIGERE
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonType
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.ForventetDokumentasjonDto
import no.nav.sosialhjelp.soknad.v2.okonomi.FormueType
import no.nav.sosialhjelp.soknad.v2.okonomi.InntektType
import no.nav.sosialhjelp.soknad.v2.okonomi.UtgiftType
import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import java.util.UUID

class ForventetDokumentasjonIntegrationTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var dokumentasjonService: DokumentasjonService

    @BeforeEach
    fun setUp() {
        opprettSoknad(id = soknadId).also { soknadRepository.save(it) }
    }

    @Test
    fun `Hente forventet dokumentasjon skal returnere lagret dokumentasjon`() {
        dokumentasjonService.opprettDokumentasjon(soknadId, InntektType.JOBB)
        dokumentasjonService.opprettDokumentasjon(soknadId, UtgiftType.UTGIFTER_BOLIGLAN)
        dokumentasjonService.opprettDokumentasjon(soknadId, FormueType.FORMUE_BRUKSKONTO)

        doGet(
            uri = getUrl(soknadId),
            responseBodyClass = ForventetDokumentasjonDto::class.java,
        )
            .also { dto ->
                assertThat(dto.dokumentasjon)
                    .hasSize(3)
                    .allMatch {
                        it.type.opplysningType == InntektType.JOBB ||
                            it.type.opplysningType == UtgiftType.UTGIFTER_BOLIGLAN ||
                            it.type.opplysningType == FormueType.FORMUE_BRUKSKONTO
                    }
            }
    }

    @Test
    fun `Oppdatere dokumentasjon som ikke finnes skal gi feil`() {
        doPutExpectError(
            uri = getUrl(UUID.randomUUID()),
            requestBody = DokumentasjonInput(DokumentasjonType.JOBB, true),
            httpStatus = HttpStatus.NOT_FOUND,
        )
    }

    @Test
    fun `Oppdatere status skal lagres i basen`() {
        dokumentasjonService.opprettDokumentasjon(soknadId, InntektType.JOBB)

        doPut(
            uri = getUrl(soknadId),
            requestBody = DokumentasjonInput(DokumentasjonType.JOBB, true),
            responseBodyClass = ForventetDokumentasjonDto::class.java,
        )
            .also { dto ->
                assertThat(dto.dokumentasjon)
                    .hasSize(1)
                    .allMatch { it.dokumentasjonStatus == LEVERT_TIDLIGERE }
            }

        dokumentasjonService.findDokumentasjonByType(soknadId, InntektType.JOBB)
            .also {
                assertThat(it).isNotNull()
                assertThat(it!!.status).isEqualTo(LEVERT_TIDLIGERE)
            }
    }

    companion object {
        private fun getUrl(soknadId: UUID): String = "/soknad/$soknadId/dokumentasjon/forventet"
    }
}
