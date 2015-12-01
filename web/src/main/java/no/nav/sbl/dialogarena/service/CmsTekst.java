package no.nav.sbl.dialogarena.service;

import org.springframework.context.*;
import org.springframework.stereotype.*;

import javax.inject.*;
import java.util.*;

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
                return String.format("KEY MANGLER: [%s]", key);
            }
        }
    }
    public boolean finnesTekst(String key, String soknadTypePrefix){
        return !getCmsTekst(key, new Object[0], soknadTypePrefix).startsWith("KEY MANGLER");
    }
}
