package no.nav.sosialhjelp.soknad.app.exceptions

import no.nav.sosialhjelp.soknad.navenhet.TjenesteUtilgjengeligException
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
    fun `skal gi 403 Forbidden ved AuthorizationException`() {
        val responseEntity =
            exceptionMapper.handleAuthorizationException(
                AuthorizationException("feil"),
            )
        assertThat(responseEntity.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
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
    fun `skal gi 400 Bad Request med BrokenSoknad ved BrokenSoknadException`() {
        val responseEntity =
            exceptionMapper.handleSoknadApiException(
                BrokenSoknadException("søknaden kan ikke sendes inn"),
            )
        assertThat(responseEntity.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(responseEntity.body?.error).isEqualTo(SoknadApiErrorType.BrokenSoknad)
    }
}
