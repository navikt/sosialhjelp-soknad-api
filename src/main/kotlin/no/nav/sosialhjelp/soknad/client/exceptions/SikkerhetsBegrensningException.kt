package no.nav.sosialhjelp.soknad.client.exceptions

import no.nav.sosialhjelp.soknad.common.exceptions.SosialhjelpSoknadApiException

class SikkerhetsBegrensningException(message: String?, exception: Exception?) :
    SosialhjelpSoknadApiException(message, exception)
