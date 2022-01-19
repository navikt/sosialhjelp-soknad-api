package no.nav.sosialhjelp.soknad.client.exceptions

import no.nav.sosialhjelp.soknad.domain.model.exception.SosialhjelpSoknadApiException

class SikkerhetsBegrensningException(message: String?, exception: Exception?) :
    SosialhjelpSoknadApiException(message, exception)
