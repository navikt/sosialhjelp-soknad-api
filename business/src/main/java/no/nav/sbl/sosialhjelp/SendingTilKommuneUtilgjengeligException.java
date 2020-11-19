package no.nav.sbl.sosialhjelp;

import no.nav.sbl.dialogarena.sendsoknad.domain.exception.SosialhjelpSoknadApiException;

public class SendingTilKommuneUtilgjengeligException extends SosialhjelpSoknadApiException {

    public SendingTilKommuneUtilgjengeligException(String message) {
        super(message);
    }
}