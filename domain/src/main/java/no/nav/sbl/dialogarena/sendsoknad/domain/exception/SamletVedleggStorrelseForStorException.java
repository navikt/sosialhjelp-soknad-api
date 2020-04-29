package no.nav.sbl.dialogarena.sendsoknad.domain.exception;

import no.nav.modig.core.exception.ApplicationException;

public class SamletVedleggStorrelseForStorException extends ApplicationException {

    public SamletVedleggStorrelseForStorException(String message, Throwable cause, String id) {
        super(message, cause, id);
    }
}
