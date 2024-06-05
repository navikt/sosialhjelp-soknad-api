package no.nav.sosialhjelp.soknad.app.exceptions

import no.nav.sosialhjelp.soknad.vedlegg.exceptions.DokumentUploadDuplicateFilename
import no.nav.sosialhjelp.soknad.vedlegg.exceptions.DokumentUploadError
import no.nav.sosialhjelp.soknad.vedlegg.exceptions.DokumentUploadUnsupportedMediaType
import no.nav.sosialhjelp.soknad.vedlegg.konvertering.FileConversionException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.net.URI

class ExceptionMapperTest {
    private val loginserviceUrl = URI("loginserviceurl")
    private val exceptionMapper = ExceptionMapper(loginserviceUrl)

    @Test
    fun `skal gi 409 Conflict ved SamtidigOppdateringException`() {
        val responseEntity = exceptionMapper.handleConflictExceptions(SamtidigOppdateringException("foo"))
        assertThat(responseEntity.statusCode).isEqualTo(HttpStatus.CONFLICT)
    }

    @Test
    fun `skal gi 415 Unsupported Media Type ved UgyldigOpplastingTypeException`() {
        val responseEntity = exceptionMapper.handleSoknadApiException(DokumentUploadUnsupportedMediaType("foo"))
        assertThat(responseEntity.statusCode).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    }

    @Test
    fun `skal gi 413 Payload Too Large ved OpplastingException`() {
        val responseEntity = exceptionMapper.handleDokumentUploadError(DokumentUploadError("foo"))
        assertThat(responseEntity.statusCode).isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE)
    }

    @Test
    fun `skal gi 403 Forbidden ved AuthorizationException`() {
        val responseEntity = exceptionMapper.handleAuthorizationException(AuthorizationException("feil"))
        assertThat(responseEntity.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
    }

    @Test
    fun `skal gi 410 Gone ved SoknadAlleredeSendtException`() {
        val responseEntity =
            exceptionMapper.handleSoknadApiException(
                SoknadAlleredeSendtException(message = "soknad allerede innsendt"),
            )
        assertThat(responseEntity.statusCode).isEqualTo(HttpStatus.GONE)
    }

    @Test
    fun `skal gi 500 med header for Ingen BigIpRedirect for andre kjente unntak`() {
        val responseEntity =
            exceptionMapper.handleSoknadApiException(
                SosialhjelpSoknadApiException(melding = "feil"),
            )
        assertThat(responseEntity.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @Test
    fun `Skal gi 502 Service Unavailable ved TjenesteUtilgjengeligException`() {
        val responseEntity =
            exceptionMapper.handleSoknadApiException(
                TjenesteUtilgjengeligException(message = "feil", throwable = RuntimeException()),
            )
        assertThat(responseEntity.statusCode).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
    }

    @Test
    fun `Skal gi 406 Not Acceptable hvis fil er lastet opp allerede`() {
        val responseEntity =
            exceptionMapper.handleSoknadApiException(
                DokumentUploadDuplicateFilename(),
            )
        assertThat(responseEntity.statusCode).isEqualTo(HttpStatus.NOT_ACCEPTABLE)
    }

    @Test
    fun `Skal gi 400 Bad Request hvis konvertering fil feiler`() {
        val responseEntity =
            exceptionMapper.handleSoknadApiException(
                FileConversionException(HttpStatus.BAD_REQUEST, "Feil ved konvertering", ""),
            )
        assertThat(responseEntity.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
    }
}
