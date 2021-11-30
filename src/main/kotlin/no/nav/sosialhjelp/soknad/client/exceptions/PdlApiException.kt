package no.nav.sosialhjelp.soknad.client.exceptions

import no.nav.sosialhjelp.soknad.domain.model.exception.SosialhjelpSoknadApiException

class PdlApiException : SosialhjelpSoknadApiException {
    constructor(message: String?) : super(message) {}
    constructor(message: String?, t: Throwable?) : super(message, t) {}
}