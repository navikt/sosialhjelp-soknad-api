package no.nav.sosialhjelp.soknad.business.exceptions;

import no.nav.sosialhjelp.soknad.domain.model.exception.SosialhjelpSoknadApiException;

public class SendingTilKommuneErMidlertidigUtilgjengeligException extends SosialhjelpSoknadApiException {

    public SendingTilKommuneErMidlertidigUtilgjengeligException(String message) {
        super(message);
    }
}
