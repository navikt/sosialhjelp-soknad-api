package no.nav.sbl.dialogarena.service;

import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;

import static no.nav.sbl.dialogarena.service.HandlebarsUtils.NO_LOCALE;

@Component
public class CmsTekst {

    @Inject
    @Named("navMessageSource")
    private MessageSource navMessageSource;

    public String getCmsTekst(String key, Object[] parameters, String soknadTypePrefix) {
        try {
            return navMessageSource.getMessage(soknadTypePrefix + "." + key, parameters, NO_LOCALE);
        } catch (NoSuchMessageException e) {
            try {
                return navMessageSource.getMessage(key, parameters, NO_LOCALE);
            } catch (NoSuchMessageException e2) {
                return String.format("KEY MANGLER: [%s]", key);
            }
        }
    }
}
