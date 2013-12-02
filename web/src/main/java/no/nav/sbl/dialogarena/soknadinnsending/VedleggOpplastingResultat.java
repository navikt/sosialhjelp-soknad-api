package no.nav.sbl.dialogarena.soknadinnsending;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;

import javax.xml.bind.annotation.XmlElement;

/**
 * Klasse for å returnere et resultat av en opplasting
 */
public class VedleggOpplastingResultat {
    private final Vedlegg vedlegg;

    public VedleggOpplastingResultat(Vedlegg vedlegg) {
        this.vedlegg = vedlegg;
    }

    public String getName() {
        return vedlegg.getNavn();
    }

    public Long getSize() {
        return vedlegg.getStorrelse();
    }

    public String getUrl() {
        return String.format("rest/soknad/%d/faktum/%d/vedlegg/%d", vedlegg.getSoknadId(), vedlegg.getFaktumId(), vedlegg.getId());
    }

    @XmlElement(name="thumbnail_url")
    public String getForhandsvisningUrl() {
        return String.format("rest/soknad/%d/faktum/%d/vedlegg/%d/thumbnail", vedlegg.getSoknadId(), vedlegg.getFaktumId(), vedlegg.getId());
    }

    @XmlElement(name="delete_url")
    public String getSletteUrl() {
        return String.format("rest/soknad/%d/faktum/%d/vedlegg/%d/delete", vedlegg.getSoknadId(), vedlegg.getFaktumId(), vedlegg.getId());
    }

    @XmlElement(name="delete_url")
    public String getSlettVerb() {
        return "POST";
    }

    public Vedlegg getVedlegg() {
        return vedlegg;
    }
}
