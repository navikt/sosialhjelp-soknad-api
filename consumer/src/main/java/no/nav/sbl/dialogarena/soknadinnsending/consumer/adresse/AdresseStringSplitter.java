package no.nav.sbl.dialogarena.soknadinnsending.consumer.adresse;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import no.nav.sbl.dialogarena.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseSokConsumer.Sokedata;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.kodeverk.KodeverkService;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;


public final class AdresseStringSplitter {
    private static final Logger log = getLogger(AdresseStringSplitter.class);

    private AdresseStringSplitter() {
        
    }
    
    static Sokedata toSokedata(String adresse) {
        return toSokedata(null, null, adresse);
    }
    
    static Sokedata toSokedata(Kodeverk kodeverk, KodeverkService kodeverkService, String adresse) {
        if (adresse == null || adresse.trim().length() <= 1) {
            return new Sokedata().withAdresse(adresse);
        }

        return firstNonNull(
            postnummerMatch(adresse),
            fullstendigGateadresseMatch(kodeverk, kodeverkService, adresse),
            new Sokedata().withAdresse(adresse)
        );
    }
    
    static Sokedata fullstendigGateadresseMatch(Kodeverk kodeverk, KodeverkService kodeverkService, String adresse) {
        final Pattern p = Pattern.compile("^([^0-9,]*) *([0-9]*)?([^,])? *,? *([0-9][0-9][0-9][0-9])? *[0-9]* *([^0-9]*[^ ])? *$");
        final Matcher m = p.matcher(adresse);
        if (m.matches()) {
            final String postnummer = m.group(4);
            final String kommunenavn = (postnummer == null) ? m.group(5) : null;
            final String kommunenummer = getKommunenummer(kodeverk,kodeverkService, kommunenavn);
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

    private static String getKommunenummer(Kodeverk kodeverk, KodeverkService kodeverkService, String kommunenavn) {
        if (kommunenavn != null && !kommunenavn.trim().isEmpty() && kodeverk != null && kodeverkService != null) {
            try  {
                return kodeverkService.gjettKommunenummer(kommunenavn);
            } catch (Exception e) {
                log.warn("KodeverkService feilet - bruker Kodeverk-WS som fallback.", e);
                return kodeverk.gjettKommunenummer(kommunenavn);
            }
        }
        return null;
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
