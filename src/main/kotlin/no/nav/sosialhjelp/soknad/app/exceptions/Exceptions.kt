package no.nav.sosialhjelp.soknad.app.exceptions

import java.time.LocalDateTime
import java.util.UUID

open class SosialhjelpSoknadApiException : RuntimeException {
    var id: String? = null
        private set

    constructor(melding: String?) : super(melding)

    @JvmOverloads
    constructor(message: String?, cause: Throwable?, id: String? = null) : super(message, cause) {
        this.id = id
    }
}

class IkkeFunnetException(melding: String?, e: Exception? = null) :
    SosialhjelpSoknadApiException(melding, e)

class SamtidigOppdateringException(message: String?) : RuntimeException(message)

class SendingTilKommuneErIkkeAktivertException(message: String?) : SosialhjelpSoknadApiException(message)

class SendingTilKommuneErMidlertidigUtilgjengeligException(message: String?) : SosialhjelpSoknadApiException(message)

class SendingTilKommuneUtilgjengeligException(message: String?) : SosialhjelpSoknadApiException(message)

class SoknadenHarNedetidException(message: String?) : SosialhjelpSoknadApiException(message)

class SoknadUnderArbeidIkkeFunnetException(message: String?) : SosialhjelpSoknadApiException(message)

class AuthorizationException(
    message: String?,
    val type: SoknadApiErrorType = SoknadApiErrorType.Forbidden,
) : SosialhjelpSoknadApiException(message)

class SoknadAlleredeSendtException(message: String?) : SosialhjelpSoknadApiException(message)

class FeilVedSendingTilFiksException(
    message: String?,
    t: Throwable?,
    id: String?,
) : SosialhjelpSoknadApiException(message, t, id)

open class SoknadLifecycleException(
    message: String?,
    t: Throwable?,
    id: UUID?,
) : SosialhjelpSoknadApiException(message, t, id.toString())

class InnsendingFeiletException(
    val deletionDate: LocalDateTime,
    message: String?,
    throwable: Throwable?,
    id: UUID?,
) : SoknadLifecycleException(message, throwable, id)
