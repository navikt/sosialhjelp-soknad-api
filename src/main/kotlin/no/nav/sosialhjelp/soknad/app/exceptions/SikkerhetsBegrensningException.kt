package no.nav.sosialhjelp.soknad.app.exceptions

class SikkerhetsBegrensningException(message: String?, exception: Exception?) :
    SosialhjelpSoknadApiException(message, exception)
