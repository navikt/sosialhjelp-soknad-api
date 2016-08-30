package no.nav.sbl.dialogarena.service;

import no.nav.sbl.dialogarena.config.ContentConfig;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Locale;

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
