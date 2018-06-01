package no.nav.sbl.dialogarena.soknadinnsending.consumer.adresse;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class AdresseStringSplitter {

    private AdresseStringSplitter() {
        
    }
    
    
    static Adressefelter toAdressefelter(String adresse) {
        if (adresse == null || adresse.trim().length() <= 1) {
            return new Adressefelter().withAdresse(adresse);
        }
        final Pattern p = Pattern.compile("^([^0-9,]*) ?([0-9]*)?([^,])?,? ?([0-9]*)? ?(.*)?$");
        final Matcher m = p.matcher(adresse);
        if (!m.matches()) {
            return new Adressefelter().withAdresse(adresse);
        }
        return new Adressefelter()
                .withAdresse(m.group(1).trim())
                .withHusnummer(m.group(2))
                .withHusbokstav(m.group(3))
                .withPostnummer(m.group(4))
                .withPoststed(m.group(5));
    }
    

    static class Adressefelter {
        String adresse;
        String husnummer;
        String husbokstav;
        String postnummer;
        String poststed;
        
        Adressefelter withAdresse(String adresse) {
            this.adresse = adresse;
            return this;
        }
        
        Adressefelter withHusnummer(String husnummer) {
            this.husnummer = husnummer;
            return this;
        }
        
        Adressefelter withHusbokstav(String husbokstav) {
            this.husbokstav = husbokstav;
            return this;
        }
        
        Adressefelter withPostnummer(String postnummer) {
            this.postnummer = postnummer;
            return this;
        }
        
        Adressefelter withPoststed(String poststed) {
            this.poststed = poststed;
            return this;
        }
    }
}
