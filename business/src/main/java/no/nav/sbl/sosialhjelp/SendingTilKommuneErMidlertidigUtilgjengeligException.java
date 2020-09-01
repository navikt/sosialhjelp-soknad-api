package no.nav.sbl.sosialhjelp;

import no.nav.sbl.dialogarena.sendsoknad.domain.exception.SosialhjelpSoknadApiException;

public class SendingTilKommuneErMidlertidigUtilgjengeligException extends SosialhjelpSoknadApiException {

    public SendingTilKommuneErMidlertidigUtilgjengeligException(String message) {
        super(message);
    }
}
