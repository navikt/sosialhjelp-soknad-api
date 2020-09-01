package no.nav.sbl.sosialhjelp;

import no.nav.sbl.dialogarena.sendsoknad.domain.exception.SosialhjelpSoknadApiException;

public class SendingTilKommuneErIkkeAktivertException extends SosialhjelpSoknadApiException {

    public SendingTilKommuneErIkkeAktivertException(String message) {
        super(message);
    }
}
