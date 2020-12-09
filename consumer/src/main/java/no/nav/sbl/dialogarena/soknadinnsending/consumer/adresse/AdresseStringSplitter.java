package no.nav.sbl.dialogarena.soknadinnsending.consumer.adresse;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseSokConsumer.Sokedata;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.kodeverk.KodeverkService;
import org.slf4j.Logger;

import static no.nav.sbl.dialogarena.soknadinnsending.consumer.adresse.AdresseSokService.isAddressTooShortOrNull;
import static org.slf4j.LoggerFactory.getLogger;


public final class AdresseStringSplitter {
    private static final Logger log = getLogger(AdresseStringSplitter.class);
    
    static Sokedata toSokedata(KodeverkService kodeverkService, String adresse) {
        if (isAddressTooShortOrNull(adresse)) {
            return new Sokedata().withAdresse(adresse);
        }

        return firstNonNull(
            postnummerMatch(adresse),
            fullstendigGateadresseMatch(kodeverkService, adresse),
            new Sokedata().withAdresse(adresse)
        );
    }
    
    static Sokedata fullstendigGateadresseMatch(KodeverkService kodeverkService, String adresse) {
        final Pattern p = Pattern.compile("^([^0-9,]*) *([0-9]*)?([^,])? *,? *([0-9][0-9][0-9][0-9])? *[0-9]* *([^0-9]*[^ ])? *$");
        final Matcher m = p.matcher(adresse);
        if (m.matches()) {
            final String postnummer = m.group(4);
            final String kommunenavn = (postnummer == null) ? m.group(5) : null;
            final String kommunenummer = getKommunenummer(kodeverkService, kommunenavn);
            final String poststed = kommunenummer == null ? m.group(5) : null;
            
            return new Sokedata()
                    .withAdresse(m.group(1).trim().replaceAll(" +", " "))
                    .withHusnummer(m.group(2))
                    .withHusbokstav(m.group(3))
                    .withPostnummer(postnummer)
                    .withPoststed(poststed)
                    .withKommunenummer(kommunenummer);
        }
        return null;
    }

    private static String getKommunenummer(KodeverkService kodeverkService, String kommunenavn) {
        return kommunenavn != null && !kommunenavn.trim().isEmpty() && kodeverkService != null ? kodeverkService.gjettKommunenummer(kommunenavn) : null;
    }

    static Sokedata postnummerMatch(String adresse) {
        final Pattern p = Pattern.compile("^ *([0-9][0-9][0-9][0-9]) *$");
        final Matcher m = p.matcher(adresse);
        if (m.matches()) {
            return new Sokedata().withPostnummer(m.group(1));
        }
        return null;
    }
    
    private static Sokedata firstNonNull(Sokedata... elems) {
        for (Sokedata e : elems) {
            if (e != null) {
                return e;
            }
        }
        return null;
    }

    
}
