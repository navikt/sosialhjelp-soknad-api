package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp;

import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.message.NavMessageSource;

public class InputSource {


    private final WebSoknad webSoknad;

    private final NavMessageSource messageSource;

    public WebSoknad getWebSoknad() {
        return webSoknad;
    }

    public NavMessageSource getMessageSource() {
        return messageSource;
    }

    public InputSource(WebSoknad webSoknad, NavMessageSource messageSource) {
        this.webSoknad = webSoknad;
        this.messageSource = messageSource;

    }
}
