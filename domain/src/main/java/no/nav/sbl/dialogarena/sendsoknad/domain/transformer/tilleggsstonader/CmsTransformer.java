package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.tilleggsstonader;

import org.apache.commons.collections15.Transformer;
import org.springframework.context.MessageSource;

import java.util.Locale;

public abstract class CmsTransformer<T, O> implements Transformer<T,O>{
    private static final Locale LOCALE = new Locale("nb", "NO");
    private final MessageSource messageSource;

    public CmsTransformer(  MessageSource navMessageSource) {
        this.messageSource = navMessageSource;
    }

    protected String cms(String key){
        return messageSource.getMessage(key, null, key, LOCALE);
    }
}
