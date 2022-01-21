package no.nav.sosialhjelp.soknad.client.exceptions

import no.nav.sosialhjelp.soknad.common.exceptions.SosialhjelpSoknadApiException

class PdlApiException : SosialhjelpSoknadApiException {
    constructor(message: String?) : super(message) {}
    constructor(message: String?, t: Throwable?) : super(message, t) {}
}
