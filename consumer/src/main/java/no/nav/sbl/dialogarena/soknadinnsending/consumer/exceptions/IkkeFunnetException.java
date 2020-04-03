package no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions;

import no.nav.sbl.dialogarena.sendsoknad.domain.exception.SosialhjelpSoknadApiException;


public class IkkeFunnetException extends SosialhjelpSoknadApiException {
    public IkkeFunnetException(String melding, Exception e) {
        super(melding, e);
    }
}
