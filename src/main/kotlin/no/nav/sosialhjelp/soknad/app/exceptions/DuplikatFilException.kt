package no.nav.sosialhjelp.soknad.app.exceptions

class DuplikatFilException(melding: String?, e: Exception? = null) :
    SosialhjelpSoknadApiException(melding, e)
