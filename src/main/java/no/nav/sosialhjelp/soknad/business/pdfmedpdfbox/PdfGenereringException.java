package no.nav.sosialhjelp.soknad.business.pdfmedpdfbox;

import no.nav.sosialhjelp.soknad.common.exceptions.SosialhjelpSoknadApiException;

public class PdfGenereringException extends SosialhjelpSoknadApiException {

    public PdfGenereringException(String melding, Throwable e) {
        super(melding, e);
    }
}
