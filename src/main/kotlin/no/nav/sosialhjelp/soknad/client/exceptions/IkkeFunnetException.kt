package no.nav.sosialhjelp.soknad.client.exceptions

import no.nav.sosialhjelp.soknad.domain.model.exception.SosialhjelpSoknadApiException

class IkkeFunnetException(melding: String?, e: Exception?) :
    SosialhjelpSoknadApiException(melding, e)
