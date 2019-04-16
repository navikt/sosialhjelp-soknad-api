package no.nav.sbl.dialogarena.soknadinnsending.consumer.adresse;

import no.nav.sbl.dialogarena.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseSokConsumer.Sokedata;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
    
    private static Sokedata fullstendigGateadresseMatch(Kodeverk kodeverk, String adresse) {
        Pattern p = Pattern.compile("^([^0-9,]*) ?([0-9]*)?([^,])?,? ?([0-9][0-9][0-9][0-9])? ?(.*)?$");
        Matcher m = p.matcher(adresse);
        if (m.matches()) {
            String postnummer = m.group(4);
            String poststed = (postnummer != null) ? m.group(5) : null;
            String kommunenavn = (postnummer == null) ? m.group(5) : null;
            String kommunenummer = (kommunenavn != null && !kommunenavn.trim().equals("") && kodeverk != null) ? kodeverk.gjettKommunenummer(kommunenavn) : null;
            
            return new Sokedata()
                    .withAdresse(m.group(1).trim())
                    .withHusnummer(m.group(2))
                    .withHusbokstav(m.group(3))
                    .withPostnummer(postnummer)
                    .withPoststed(poststed)
                    .withKommunenummer(kommunenummer);
        }
        return null;
    }

    private static Sokedata postnummerMatch(String adresse) {
        Pattern p = Pattern.compile("^([0-9][0-9][0-9][0-9]) *$");
        Matcher m = p.matcher(adresse);
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
