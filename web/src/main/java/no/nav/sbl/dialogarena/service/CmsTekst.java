package no.nav.sbl.dialogarena.service;

import no.nav.sbl.dialogarena.config.ContentConfig;
import no.nav.sbl.dialogarena.sendsoknad.domain.message.NavMessageSource;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Locale;
import java.util.Map;

@Component
public class CmsTekst {

    @Inject
    @Named("navMessageBundles")
    private ContentConfig.NavMessageWrapper navMessageBundles;

    public String getCmsTekst(String key, Object[] parameters, String soknadTypePrefix, Locale locale) {
        try {
            return navMessageBundles.get(soknadTypePrefix).getMessage(soknadTypePrefix + "." + key, parameters, locale);
        } catch (NoSuchMessageException e) {
            try {
                return navMessageBundles.get(soknadTypePrefix).getMessage(key, parameters, locale);
            } catch (NoSuchMessageException e2) {
                return null;
            }
        }
    }
}
