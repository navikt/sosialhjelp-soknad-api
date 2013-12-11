package no.nav.sbl.dialogarena.soknadinnsending.business.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Klasse for å vise forventede vedlegg for en innsending
 */
public class VedleggForventning {
    private Faktum faktum;
    private Vedlegg vedlegg;
    private String gosysId;

    public VedleggForventning() {
    }

    public VedleggForventning(Faktum faktum, Vedlegg vedlegg, String gosysId) {
        this.faktum = faktum;
        this.vedlegg = vedlegg;
        this.gosysId = gosysId;
        if (faktum.getInnsendingsvalg() == null || faktum.getInnsendingsvalg().equals(Faktum.Status.IkkeVedlegg)) {
            faktum.setInnsendingsvalg(Faktum.Status.VedleggKreves);
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

    public String getGosysId() {
        return gosysId;
    }

    public void setGosysId(String gosysId) {
        this.gosysId = gosysId;
    }
}
