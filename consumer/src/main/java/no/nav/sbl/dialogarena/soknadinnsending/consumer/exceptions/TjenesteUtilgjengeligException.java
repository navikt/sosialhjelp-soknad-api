package no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions;

import no.nav.sbl.dialogarena.sendsoknad.domain.exception.SosialhjelpSoknadApiException;

public class TjenesteUtilgjengeligException extends SosialhjelpSoknadApiException {
    public TjenesteUtilgjengeligException(String message, Exception exception) {
        super(message, exception);
    }
}
