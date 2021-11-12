package no.nav.sosialhjelp.soknad.business.service.adressesok;


import no.nav.sosialhjelp.soknad.client.kodeverk.KodeverkService;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class AdresseStringSplitter {

    private AdresseStringSplitter() {
    }

    public static Sokedata toSokedata(KodeverkService kodeverkService, String adresse) {
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
        Pattern p = Pattern.compile("^([^0-9,]*) *([0-9]*)?([^,])? *,? *([0-9][0-9][0-9][0-9])? *[0-9]* *([^0-9]*[^ ])? *$");
        Matcher m = p.matcher(adresse);
        if (m.matches()) {
            String postnummer = m.group(4);
            String kommunenavn = (postnummer == null) ? m.group(5) : null;
            String kommunenummer = getKommunenummer(kodeverkService, kommunenavn);
            String poststed = kommunenummer == null ? m.group(5) : null;
            String gateAdresse = m.group(1).trim().replaceAll(" +", " ");

            return new Sokedata()
                    .withAdresse(gateAdresse)
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

    public static boolean isAddressTooShortOrNull(String address) {
        return address == null || address.trim().length() < 2;
    }
}
