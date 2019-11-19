package no.nav.sbl.dialogarena.soknadinnsending.consumer.adresse;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import no.nav.sbl.dialogarena.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseSokConsumer.Sokedata;


public final class AdresseStringSplitter {

    private AdresseStringSplitter() {
        
    }
    
    static Sokedata toSokedata(String adresse) {
        return toSokedata(null, adresse);
    }
    
    static Sokedata toSokedata(Kodeverk kodeverk, String adresse) {
        if (adresse == null || adresse.trim().length() <= 1) {
            return new Sokedata().withAdresse(adresse);
        }

        return firstNonNull(
            postnummerMatch(adresse),
            fullstendigGateadresseMatch(kodeverk, adresse),
            new Sokedata().withAdresse(adresse)
        );
    }
    
    static Sokedata fullstendigGateadresseMatch(Kodeverk kodeverk, String adresse) {
        final Pattern p = Pattern.compile("^([^0-9,]*) *([0-9]*)?([^,])? *,? *([0-9][0-9][0-9][0-9])? *[0-9]* *([^0-9]*[^ ])? *$");
        final Matcher m = p.matcher(adresse);
        if (m.matches()) {
            final String postnummer = m.group(4);
            final String kommunenavn = (postnummer == null) ? m.group(5) : null;
            final String kommunenummer = (kommunenavn != null && !kommunenavn.trim().isEmpty() && kodeverk != null) ? kodeverk.gjettKommunenummer(kommunenavn) : null;
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
