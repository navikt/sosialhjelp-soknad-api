package no.nav.sosialhjelp.soknad.pdf

import no.nav.sosialhjelp.soknad.common.exceptions.SosialhjelpSoknadApiException

class PdfGenereringException(melding: String?, e: Throwable?) : SosialhjelpSoknadApiException(melding, e)
