package no.nav.sosialhjelp.soknad.business.exceptions;

import no.nav.sosialhjelp.soknad.domain.model.exception.SosialhjelpSoknadApiException;

public class SoknadenHarNedetidException extends SosialhjelpSoknadApiException {

    public SoknadenHarNedetidException(String message) {
        super(message);
    }
}
