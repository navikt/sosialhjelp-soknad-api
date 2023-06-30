package no.nav.sosialhjelp.soknad.app.exceptions

class DuplikatFilException(message: String?, e: Exception? = null) :
    SosialhjelpSoknadApiException(message, e)
