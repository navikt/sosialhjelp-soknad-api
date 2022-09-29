package no.nav.sosialhjelp.soknad.app.exceptions

class PdlApiException : SosialhjelpSoknadApiException {
    constructor(message: String?) : super(message)
    constructor(message: String?, t: Throwable?) : super(message, t)
}
