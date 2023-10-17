package no.nav.sosialhjelp.soknad.vedlegg.exceptions

import no.nav.sosialhjelp.soknad.app.exceptions.SosialhjelpSoknadApiException
import org.springframework.web.bind.annotation.ResponseStatus

class OpplastingException(message: String?, cause: Throwable?, id: String?) :
    SosialhjelpSoknadApiException(message, cause, id)

class SamletVedleggStorrelseForStorException(message: String?, cause: Throwable?, id: String?) :
    SosialhjelpSoknadApiException(message, cause, id)

class UgyldigOpplastingTypeException(message: String?, cause: Throwable?, id: String?) :
    SosialhjelpSoknadApiException(message, cause, id)

class KonverteringTilPdfException(message: String?, cause: Throwable?, id: String? = null) :
    SosialhjelpSoknadApiException(message, cause, id)

class DuplikatFilException(message: String?, e: Exception? = null) :
    SosialhjelpSoknadApiException(message, e)
