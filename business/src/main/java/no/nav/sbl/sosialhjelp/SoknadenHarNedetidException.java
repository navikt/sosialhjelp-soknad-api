package no.nav.sbl.sosialhjelp;

import no.nav.sbl.dialogarena.sendsoknad.domain.exception.SosialhjelpSoknadApiException;

public class SoknadenHarNedetidException extends SosialhjelpSoknadApiException {

    public SoknadenHarNedetidException(String message) {
        super(message);
    }
}
