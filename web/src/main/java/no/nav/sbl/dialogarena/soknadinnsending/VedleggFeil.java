package no.nav.sbl.dialogarena.soknadinnsending;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Klasse for å returnere et resultat av en opplasting
 */
@XmlRootElement
public class VedleggFeil {

    private final String kode;

    public VedleggFeil(String kode) {
        this.kode = kode;
    }

    public String getKode() {
        return kode;
    }
}
