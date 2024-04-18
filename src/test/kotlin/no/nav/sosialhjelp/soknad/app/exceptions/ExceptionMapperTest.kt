package no.nav.sosialhjelp.soknad.app.exceptions

import no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering.exception.ExcelKonverteringException
import no.nav.sosialhjelp.soknad.vedlegg.exceptions.DuplikatFilException
import no.nav.sosialhjelp.soknad.vedlegg.exceptions.KonverteringTilPdfException
import no.nav.sosialhjelp.soknad.vedlegg.exceptions.OpplastingException
import no.nav.sosialhjelp.soknad.vedlegg.exceptions.UgyldigOpplastingTypeException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class ExceptionMapperTest {
    private val loginserviceUrl = "loginserviceurl"
    private val exceptionMapper = ExceptionMapper(loginserviceUrl)

    @Test
    fun `skal gi 409 Conflict ved SamtidigOppdateringException`() {
        val responseEntity =
            exceptionMapper.handleThrowable(
                SamtidigOppdateringException(message = "Mulig versjonskonflikt..."),
            )
        assertThat(responseEntity.statusCode).isEqualTo(HttpStatus.CONFLICT)
    }

    @Test
    fun `skal gi 415 Unsupported Media Type ved UgyldigOpplastingTypeException`() {
        val responseEntity =
            exceptionMapper.handleSoknadApiException(
                UgyldigOpplastingTypeException(message = "feil", cause = RuntimeException(), id = "id"),
            )
        assertThat(responseEntity.statusCode).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    }

    @Test
    fun `skal gi 413 Payload Too Large ved OpplastingException`() {
        val responseEntity =
            exceptionMapper.handleSoknadApiException(
                OpplastingException(message = "feil", cause = RuntimeException(), id = "id"),
            )
        assertThat(responseEntity.statusCode).isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE)
    }

    @Test
    fun `skal gi 403 Forbidden ved AuthorizationException`() {
        val responseEntity =
            exceptionMapper.handleSoknadApiException(
                AuthorizationException(message = "feil"),
            )
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
        assertThat(responseEntity.headers.getFirst(Feilmelding.NO_BIGIP_5XX_REDIRECT)).isEqualTo("true")
    }

    @Test
    fun `skal gi 500 med header for ingen BigIpRedirect ved TjenesteUtilgjengeligException`() {
        val responseEntity =
            exceptionMapper.handleSoknadApiException(
                TjenesteUtilgjengeligException(message = "feil", throwable = RuntimeException()),
            )
        assertThat(responseEntity.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        assertThat(responseEntity.headers.getFirst(Feilmelding.NO_BIGIP_5XX_REDIRECT)).isEqualTo("true")
    }

    @Test
    fun `skal gi 500 med header for ingen BigIpRedirect ved EttersendelseSendtForSentException`() {
        val responseEntity =
            exceptionMapper.handleSoknadApiException(
                EttersendelseSendtForSentException(message = "feil"),
            )
        assertThat(responseEntity.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        assertThat(responseEntity.headers.getFirst(Feilmelding.NO_BIGIP_5XX_REDIRECT)).isEqualTo("true")
    }

    @Test
    fun `Skal gi 406 Not Acceptable hvis fil er lastet opp allerede`() {
        val responseEntity =
            exceptionMapper.handleSoknadApiException(
                DuplikatFilException(message = "feil"),
            )
        assertThat(responseEntity.statusCode).isEqualTo(HttpStatus.NOT_ACCEPTABLE)
    }

    @Test
    fun `Skal gi 500 Internal Server Error hvis konvertering av akseptert fil feiler`() {
        val responseEntity =
            exceptionMapper.handleSoknadApiException(
                KonverteringTilPdfException(message = "feil", cause = ExcelKonverteringException("feil", null)),
            )
        assertThat(responseEntity.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
    }
}
