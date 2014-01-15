package no.nav.sbl.dialogarena.soknadinnsending;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Klasse for å returnere et resultat av en opplasting
 */
@XmlRootElement
public class RestFeil {

    private final String kode;

    public RestFeil(String kode) {
        this.kode = kode;
    }

    public String getKode() {
        return kode;
    }
}
