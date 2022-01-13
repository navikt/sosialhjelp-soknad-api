package no.nav.sosialhjelp.soknad.common.rest.feil

import no.nav.sosialhjelp.soknad.client.exceptions.SikkerhetsBegrensningException
import no.nav.sosialhjelp.soknad.client.exceptions.TjenesteUtilgjengeligException
import no.nav.sosialhjelp.soknad.common.rest.feil.Feilmelding.Companion.NO_BIGIP_5XX_REDIRECT
import no.nav.sosialhjelp.soknad.domain.model.exception.AuthorizationException
import no.nav.sosialhjelp.soknad.domain.model.exception.EttersendelseSendtForSentException
import no.nav.sosialhjelp.soknad.domain.model.exception.OpplastingException
import no.nav.sosialhjelp.soknad.domain.model.exception.SosialhjelpSoknadApiException
import no.nav.sosialhjelp.soknad.domain.model.exception.UgyldigOpplastingTypeException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ApplicationExceptionMapperTest {

    val mapper = ApplicationExceptionMapper()

    @Test
    fun skalGi415UnsupportedMediaTypeVedUgyldigOpplastingTypeException() {
        val response = mapper.toResponse(UgyldigOpplastingTypeException("feil", RuntimeException(), "id"))
        assertThat(response.status).isEqualTo(415)
    }

    @Test
    fun skalGi413RequestTooLongVedOpplastingException() {
        val response = mapper.toResponse(OpplastingException("feil", RuntimeException(), "id"))
        assertThat(response.status).isEqualTo(413)
    }

    @Test
    fun skalGi403ForbiddenVedAuthorizationException() {
        val response = mapper.toResponse(AuthorizationException("feil"))
        assertThat(response.status).isEqualTo(403)
    }

    @Test
    fun skalGi500MedHeaderForIngenBigIpRedirectForAndreKjenteUnntak() {
        val response = mapper.toResponse(SosialhjelpSoknadApiException("feil"))
        assertThat(response.status).isEqualTo(500)
        assertThat(response.getHeaderString(NO_BIGIP_5XX_REDIRECT)).isEqualTo("true")
    }

    @Test
    fun skalGi500MedHeaderForIngenBigIpRedirectVedTjenesteUtilgjengeligException() {
        val response = mapper.toResponse(TjenesteUtilgjengeligException("feil", RuntimeException()))
        assertThat(response.status).isEqualTo(500)
        assertThat(response.getHeaderString(NO_BIGIP_5XX_REDIRECT)).isEqualTo("true")
    }

    @Test
    fun skalGi500MedHeaderForIngenBigIpRedirectVedEttersendelseSendtForSentException() {
        val response = mapper.toResponse(EttersendelseSendtForSentException("feil"))
        assertThat(response.status).isEqualTo(500)
        assertThat(response.getHeaderString(NO_BIGIP_5XX_REDIRECT)).isEqualTo("true")
    }

    @Test
    fun skalGi500VMedHeaderForIngenBigIpRedirectedSikkerhetsBegrensningException() {
        val response = mapper.toResponse(SikkerhetsBegrensningException("feil", RuntimeException()))
        assertThat(response.status).isEqualTo(500)
        assertThat(response.getHeaderString(NO_BIGIP_5XX_REDIRECT)).isEqualTo("true")
    }
}
