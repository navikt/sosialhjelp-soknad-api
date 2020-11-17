package no.nav.sbl.sosialhjelp.pdfmedpdfbox;

import no.nav.sbl.dialogarena.sendsoknad.domain.exception.SosialhjelpSoknadApiException;

public class PdfGenereringException extends SosialhjelpSoknadApiException {

    public PdfGenereringException(String melding, Throwable e) {
        super(melding, e);
    }
}
