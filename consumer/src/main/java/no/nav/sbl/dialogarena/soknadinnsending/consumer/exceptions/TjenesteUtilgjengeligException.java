package no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions;

import no.nav.sosialhjelp.soknad.domain.model.exception.SosialhjelpSoknadApiException;

public class TjenesteUtilgjengeligException extends SosialhjelpSoknadApiException {
    public TjenesteUtilgjengeligException(String message, Exception exception) {
        super(message, exception);
    }
}
