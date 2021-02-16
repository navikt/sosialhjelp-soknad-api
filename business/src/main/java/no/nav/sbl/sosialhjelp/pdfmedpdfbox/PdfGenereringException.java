package no.nav.sbl.sosialhjelp.pdfmedpdfbox;

import no.nav.sosialhjelp.soknad.domain.model.exception.SosialhjelpSoknadApiException;

public class PdfGenereringException extends SosialhjelpSoknadApiException {

    public PdfGenereringException(String melding, Throwable e) {
        super(melding, e);
    }
}
