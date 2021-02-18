package no.nav.sosialhjelp.soknad.business.pdfmedpdfbox;

import no.nav.sosialhjelp.soknad.domain.model.exception.SosialhjelpSoknadApiException;

public class PdfGenereringException extends SosialhjelpSoknadApiException {

    public PdfGenereringException(String melding, Throwable e) {
        super(melding, e);
    }
}
