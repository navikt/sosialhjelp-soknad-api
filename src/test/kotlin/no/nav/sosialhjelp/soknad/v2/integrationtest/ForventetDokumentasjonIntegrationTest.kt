package no.nav.sosialhjelp.soknad.v2.integrationtest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentRef
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonInput
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonService
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonStatus.LASTET_OPP
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonStatus.LEVERT_TIDLIGERE
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentlagerService
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.ForventetDokumentasjonDto
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.InntektTypeDto
import no.nav.sosialhjelp.soknad.v2.okonomi.formue.FormueType
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.InntektType
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.UtgiftType
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

    @MockkBean
    private lateinit var dokumentlagerService: DokumentlagerService

    private lateinit var soknadId: UUID

    @BeforeEach
    fun setUp() {
        soknadId = soknadRepository.save(opprettSoknad()).id
        every { dokumentlagerService.deleteDokument(soknadId, any()) } just runs
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
                        it.type.value == InntektType.JOBB ||
                            it.type.value == UtgiftType.UTGIFTER_BOLIGLAN ||
                            it.type.value == FormueType.FORMUE_BRUKSKONTO
                    }
            }
    }

    @Test
    fun `Oppdatere dokumentasjon som ikke finnes skal gi feil`() {
        doPutExpectError(
            uri = getUrl(soknadId),
            requestBody = DokumentasjonInput(InntektTypeDto(InntektType.JOBB), true),
            soknadId = soknadId,
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR,
        )
    }

    @Test
    fun `Oppdatere status skal lagres i basen`() {
        dokumentasjonService.opprettDokumentasjon(soknadId, InntektType.JOBB)

        doPut(
            uri = getUrl(soknadId),
            requestBody = DokumentasjonInput(InntektTypeDto(InntektType.JOBB), true),
            responseBodyClass = ForventetDokumentasjonDto::class.java,
            soknadId = soknadId,
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

    @Test
    fun `Sette status til LEVERT TIDLIGERE skal fjerne alle dokumenter`() {
        dokumentasjonService.opprettDokumentasjon(soknadId, InntektType.JOBB)
            .run {
                copy(
                    status = LASTET_OPP,
                    dokumenter =
                        setOf(
                            DokumentRef(UUID.randomUUID(), "fil1.pdf"),
                            DokumentRef(UUID.randomUUID(), "fil2.pdf"),
                        ),
                )
            }
            .also { dokumentasjonService.updateDokumentasjon(it) }

        doGet(uri = getUrl(soknadId), responseBodyClass = ForventetDokumentasjonDto::class.java)
            .also { dto ->
                assertThat(dto.dokumentasjon)
                    .hasSize(1)
                    .allMatch { it.dokumenter.size == 2 }
            }

        doPut(
            uri = getUrl(soknadId),
            requestBody = DokumentasjonInput(InntektTypeDto(InntektType.JOBB), true),
            responseBodyClass = ForventetDokumentasjonDto::class.java,
            soknadId,
        ).also { dto -> assertThat(dto.dokumentasjon.first().dokumenter).isEmpty() }

        dokumentasjonService.findDokumentasjonByType(soknadId, InntektType.JOBB)
            .also { assertThat(it!!.dokumenter).isEmpty() }
    }

    companion object {
        private fun getUrl(soknadId: UUID): String = "/soknad/$soknadId/dokumentasjon/forventet"
    }
}
