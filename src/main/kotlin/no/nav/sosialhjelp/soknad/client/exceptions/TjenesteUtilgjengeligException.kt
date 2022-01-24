package no.nav.sosialhjelp.soknad.client.exceptions

import no.nav.sosialhjelp.soknad.common.exceptions.SosialhjelpSoknadApiException

class TjenesteUtilgjengeligException(message: String, throwable: Throwable?) :
    SosialhjelpSoknadApiException(message, throwable)
