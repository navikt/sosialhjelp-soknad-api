package no.nav.sosialhjelp.soknad.client.exceptions

import no.nav.sosialhjelp.soknad.common.exceptions.SosialhjelpSoknadApiException

class IkkeFunnetException(melding: String?, e: Exception?) :
    SosialhjelpSoknadApiException(melding, e)
