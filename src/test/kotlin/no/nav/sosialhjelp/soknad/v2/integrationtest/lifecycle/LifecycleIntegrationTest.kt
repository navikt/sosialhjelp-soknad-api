package no.nav.sosialhjelp.soknad.v2.integrationtest.lifecycle

import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpObjectMapper
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad
import no.nav.sosialhjelp.soknad.app.exceptions.SoknadApiError
import no.nav.sosialhjelp.soknad.v2.SoknadSendtDto
import no.nav.sosialhjelp.soknad.v2.StartSoknadResponseDto
import no.nav.sosialhjelp.soknad.v2.familie.FamilieRepository
import no.nav.sosialhjelp.soknad.v2.kontakt.AdresseValg
import no.nav.sosialhjelp.soknad.v2.kontakt.NavEnhet
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringDokumentInfo
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringDto
import no.nav.sosialhjelp.soknad.vedlegg.filedetection.FileDetectionUtils
import no.nav.sosialhjelp.soknad.vedlegg.filedetection.MimeTypes
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import java.time.LocalDate
import java.util.UUID

class LifecycleIntegrationTest : SetupLifecycleIntegrationTest() {
    @Autowired
    private lateinit var familieRepository: FamilieRepository

    @Test
    fun `Opprette soknad skal generere soknads-objekt og hente register-data`() {
        createNewSoknad()
            .also { soknadId -> assertThat(soknadId).isInstanceOf(UUID::class.java) }
            .also { soknadId ->
                assertThat(soknadRepository.findByIdOrNull(soknadId)).isNotNull
                assertThat(metadataRepository.findByIdOrNull(soknadId)).isNotNull
                assertThat(eierRepository.findByIdOrNull(soknadId)).isNotNull
                assertThat(kontaktRepository.findByIdOrNull(soknadId)).isNotNull
                assertThat(familieRepository.findByIdOrNull(soknadId)).isNotNull
            }
    }

    @Test
    fun `Slette soknad skal fjerne soknad`() {
        val soknadId = createNewSoknad()

        every { mellomlagringClient.hentDokumenterMetadata(soknadId.toString()) } returns createMellomlagringDto(soknadId)
        every { mellomlagringClient.slettAlleDokumenter(soknadId.toString()) } just runs

        doDelete(uri = deleteUri(soknadId), soknadId = soknadId)

        assertThat(soknadRepository.findByIdOrNull(soknadId)).isNull()
        verify(exactly = 1) { mellomlagringClient.slettAlleDokumenter(soknadId.toString()) }
    }

    @Test
    fun `Sende soknad skal avslutte soknad i db`() {
        val soknadId = createNewSoknad()

        every { mellomlagringClient.hentDokumenterMetadata(any()) } returns
            MellomlagringDto(soknadId.toString(), emptyList())

        kontaktRepository.findByIdOrNull(soknadId)!!
            .run {
                copy(
                    adresser = adresser.copy(adressevalg = AdresseValg.FOLKEREGISTRERT),
                    mottaker = createNavEnhet(),
                )
            }
            .also { kontaktRepository.save(it) }

        doPost(
            uri = sendUri(soknadId),
            responseBodyClass = SoknadSendtDto::class.java,
            soknadId = soknadId,
        )
            .also { dto ->
                assertThat(dto.digisosId).isNotEqualTo(soknadId)
                assertThat(dto.tidspunkt.toLocalDate())
                    .isEqualTo(LocalDate.now())
            }

        assertCapturedValues()
        metadataRepository.findByIdOrNull(soknadId)!!
            .let { assertThat(it.status).isEqualTo(SoknadStatus.SENDT) }
    }

    // TODO Er dette riktig antakelse?
    @Test
    fun `Exception i fetcher med ContinueOnError = true skal ikke stoppe opprettelse av ny soknad`() {
        every { krrService.getMobilnummer(any()) } throws IllegalArgumentException("Feil ved henting av telefonnummer")

        createNewSoknad().also { soknadId ->
            soknadRepository.findByIdOrNull(soknadId).let { assertThat(it).isNotNull }
        }
    }

    @Test
    fun `Exception i fetcher med ContinueOnError = false skal stoppe innhenting og ingenting skal lagres`() {
        every { personService.hentPerson(any()) } throws IllegalArgumentException("Feil ved henting av person")

        doPostFullResponse(uri = createUri)
            .expectStatus().is5xxServerError
            .expectBody(SoknadApiError::class.java)

        soknadRepository.findAll().let { assertThat(it).isEmpty() }
    }

    @Test
    fun `Feil ved sending til FIKS skal sette status FEILET`() {
        val soknadId = createNewSoknad()

        every { mellomlagringClient.hentDokumenterMetadata(any()) } returns
            MellomlagringDto(soknadId.toString(), emptyList())
        every { digisosApiV2Client.krypterOgLastOppFiler(any(), any(), any(), any(), any(), any(), any()) } throws
            RuntimeException("Noe feilet")

        kontaktRepository.findByIdOrNull(soknadId)!!
            .run {
                copy(
                    adresser = adresser.copy(adressevalg = AdresseValg.FOLKEREGISTRERT),
                    mottaker = createNavEnhet(),
                )
            }
            .also { kontaktRepository.save(it) }

        doPostExpectError(
            uri = sendUri(soknadId),
            requestBody = "",
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR,
            soknadId = soknadId,
        )

        metadataRepository.findByIdOrNull(soknadId)!!
            .also { assertThat(it.status).isEqualTo(SoknadStatus.INNSENDING_FEILET) }
    }

    private fun createNewSoknad(): UUID {
        return doPost(
            uri = createUri,
            responseBodyClass = StartSoknadResponseDto::class.java,
        )
            .soknadId
    }

    private fun createNavEnhet(): NavEnhet {
        return NavEnhet(
            "navEnhet",
            "1234456",
            createVegadresse().kommunenummer,
            "12345678",
            "kommunen",
        )
    }

    private fun assertCapturedValues() {
        with(CapturedValues) {
            assertSoknadJson()
            assertTilleggsinformasjon()
            assertDokumenterIsPdf()
        }
    }

    private fun CapturedValues.assertSoknadJson() {
        objectMapper.readValue(soknadJsonSlot.captured, JsonSoknad::class.java)
            .also { jsonSoknad ->
                assertThat(jsonSoknad.data.personalia.personIdentifikator.verdi).isEqualTo(userId)
                assertThat(jsonSoknad.mottaker.enhetsnummer).isNotNull()
            }
    }

    private fun CapturedValues.assertDokumenterIsPdf() {
        dokumenterSlot.captured.forEach {
            assertThat(FileDetectionUtils.detectMimeType(it.data.readAllBytes())).isEqualTo(MimeTypes.APPLICATION_PDF)
        }
    }

    private fun CapturedValues.assertTilleggsinformasjon() {
        assertThat(tilleggsinformasjonSlot.captured).contains(createNavEnhet().enhetsnummer)
    }

    companion object {
        private val createUri = "/soknad/create"

        private fun deleteUri(soknadId: UUID) = "/soknad/$soknadId/delete"

        private fun sendUri(soknadId: UUID) = "/soknad/$soknadId/send"

        private val objectMapper = JsonSosialhjelpObjectMapper.createObjectMapper()
    }
}

private fun createMellomlagringDto(soknadId: UUID): MellomlagringDto {
    return MellomlagringDto(
        navEksternRefId = soknadId.toString(),
        mellomlagringMetadataList =
            listOf(
                MellomlagringDokumentInfo(
                    filId = UUID.randomUUID().toString(),
                    filnavn = "filnavn.pdf",
                    storrelse = 1234L,
                    mimetype = MimeTypes.APPLICATION_PDF,
                ),
            ),
    )
}
