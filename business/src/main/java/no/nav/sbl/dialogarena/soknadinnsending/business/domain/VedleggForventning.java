package no.nav.sbl.dialogarena.soknadinnsending.business.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Klasse for å vise forventede vedlegg for en innsending
 */
public class VedleggForventning {
    private Faktum faktum;
    private Vedlegg vedlegg;
    private String skjemaNummer;
    private String property;

    public VedleggForventning() {
    }

    public VedleggForventning(Faktum faktum, Vedlegg vedlegg, String skjemaNummer, String property) {
        this.faktum = faktum;
        this.vedlegg = vedlegg;
        this.skjemaNummer = skjemaNummer;
        this.property = property;
        if (faktum.getInnsendingsvalg(skjemaNummer).equals(Faktum.Status.IkkeVedlegg)) {
            faktum.setInnsendingsvalg(skjemaNummer, Faktum.Status.VedleggKreves);
        }
    }

    @JsonProperty
    public Faktum getFaktum() {
        return faktum;
    }

    public void setFaktum(Faktum faktum) {
        this.faktum = faktum;
    }

    public Vedlegg getVedlegg() {
        return vedlegg;
    }

    public void setVedlegg(Vedlegg vedlegg) {
        this.vedlegg = vedlegg;
    }

    public String getskjemaNummer() {
        return skjemaNummer;
    }

    public void setskjemaNummer(String skjemaNummer) {
        this.skjemaNummer = skjemaNummer;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }
}
