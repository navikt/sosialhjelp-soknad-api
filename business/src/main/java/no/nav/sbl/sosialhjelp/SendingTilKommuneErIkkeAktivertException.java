package no.nav.sbl.sosialhjelp;

import no.nav.sosialhjelp.soknad.domain.model.exception.SosialhjelpSoknadApiException;

public class SendingTilKommuneErIkkeAktivertException extends SosialhjelpSoknadApiException {

    public SendingTilKommuneErIkkeAktivertException(String message) {
        super(message);
    }
}
