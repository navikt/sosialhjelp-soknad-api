package no.nav.sosialhjelp.soknad.business.exceptions;

import no.nav.sosialhjelp.soknad.domain.model.exception.SosialhjelpSoknadApiException;

public class SendingTilKommuneErIkkeAktivertException extends SosialhjelpSoknadApiException {

    public SendingTilKommuneErIkkeAktivertException(String message) {
        super(message);
    }
}
