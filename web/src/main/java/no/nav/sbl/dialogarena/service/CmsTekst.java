package no.nav.sbl.dialogarena.service;

import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Locale;

@Component
public class CmsTekst {

    @Inject
    @Named("navMessageSource")
    private MessageSource navMessageSource;

    public String getCmsTekst(String key, Object[] parameters, String soknadTypePrefix, Locale locale) {
        try {
            return navMessageSource.getMessage(soknadTypePrefix + "." + key, parameters, locale);
        } catch (NoSuchMessageException e) {
            try {
                return navMessageSource.getMessage(key, parameters, locale);
            } catch (NoSuchMessageException e2) {
                return "";
            }
        }
    }
    public boolean finnesTekst(String key, String soknadTypePrefix, Locale locale){
        return !getCmsTekst(key, new Object[0], soknadTypePrefix, locale).isEmpty();
    }
}
