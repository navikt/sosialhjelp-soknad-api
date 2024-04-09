package no.nav.sosialhjelp.soknad.app.exceptions

open class SosialhjelpSoknadApiException : RuntimeException {
    var id: String? = null
        private set

    constructor(melding: String?) : super(melding)

    @JvmOverloads
    constructor(message: String?, cause: Throwable?, id: String? = null) : super(message, cause) {
        this.id = id
    }
}

class SamtidigOppdateringException(message: String?) : RuntimeException(message)

class SendingTilKommuneErIkkeAktivertException(message: String?) : SosialhjelpSoknadApiException(message)

class SendingTilKommuneErMidlertidigUtilgjengeligException(message: String?) : SosialhjelpSoknadApiException(message)

class SendingTilKommuneUtilgjengeligException(message: String?) : SosialhjelpSoknadApiException(message)

class SoknadenHarNedetidException(message: String?) : SosialhjelpSoknadApiException(message)

class SoknadLaastException(message: String?) : RuntimeException(message)

class SoknadUnderArbeidIkkeFunnetException(message: String?) : SosialhjelpSoknadApiException(message)

class AuthorizationException(message: String?) : SosialhjelpSoknadApiException(message)

class SoknadAlleredeSendtException(message: String?) : SosialhjelpSoknadApiException(message)

class EttersendelseSendtForSentException(message: String?) : SosialhjelpSoknadApiException(message)

class FeilVedSendingTilFiksException(
    message: String?,
    e: Exception?,
    id: String?
) : SosialhjelpSoknadApiException(message, e, id)
