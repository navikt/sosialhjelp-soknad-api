package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp;

import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import org.springframework.context.MessageSource;

public class InputSource {


    private final WebSoknad webSoknad;

    private final MessageSource messageSource;

    public WebSoknad getWebSoknad() {
        return webSoknad;
    }

    public MessageSource getMessageSource() {
        return messageSource;
    }

    public InputSource(WebSoknad webSoknad, MessageSource messageSource) {
        this.webSoknad = webSoknad;
        this.messageSource = messageSource;

    }
}
