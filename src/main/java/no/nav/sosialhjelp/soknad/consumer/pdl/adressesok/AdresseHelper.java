package no.nav.sosialhjelp.soknad.consumer.pdl.adressesok;

import org.apache.commons.text.WordUtils;

import java.util.Arrays;
import java.util.stream.Collectors;

public final class AdresseHelper {

    private AdresseHelper() {
    }

    public static String formatterKommunenavn(String kommunenavn) {
        if (kommunenavn == null) {
            return null;
        }
        return Arrays.stream(kommunenavn.toLowerCase().split(" "))
                .map(s -> !s.equals("og") ? WordUtils.capitalize(s, '-') : s)
                .collect(Collectors.joining(" "));
    }
}
