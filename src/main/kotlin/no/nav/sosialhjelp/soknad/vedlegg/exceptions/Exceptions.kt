package no.nav.sosialhjelp.soknad.vedlegg.exceptions

import no.nav.sosialhjelp.soknad.common.exceptions.SosialhjelpSoknadApiException

class OpplastingException(message: String?, cause: Throwable?, id: String?) :
    SosialhjelpSoknadApiException(message, cause, id)

class SamletVedleggStorrelseForStorException(message: String?, cause: Throwable?, id: String?) :
    SosialhjelpSoknadApiException(message, cause, id)

class UgyldigOpplastingTypeException(message: String?, cause: Throwable?, id: String?) :
    SosialhjelpSoknadApiException(message, cause, id)
