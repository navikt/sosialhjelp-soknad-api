package no.nav.sosialhjelp.soknad.app.exceptions

class IkkeFunnetException(melding: String?, e: Exception? = null) :
    SosialhjelpSoknadApiException(melding, e)
