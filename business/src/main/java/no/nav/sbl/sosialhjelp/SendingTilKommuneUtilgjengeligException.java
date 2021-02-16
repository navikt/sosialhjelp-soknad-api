package no.nav.sbl.sosialhjelp;

import no.nav.sosialhjelp.soknad.domain.model.exception.SosialhjelpSoknadApiException;

public class SendingTilKommuneUtilgjengeligException extends SosialhjelpSoknadApiException {

    public SendingTilKommuneUtilgjengeligException(String message) {
        super(message);
    }
}