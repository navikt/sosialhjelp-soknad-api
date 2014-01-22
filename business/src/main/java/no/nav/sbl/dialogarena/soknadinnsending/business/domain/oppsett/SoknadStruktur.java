package no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett;

import org.apache.commons.collections15.Predicate;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static no.nav.modig.lang.collections.IterUtils.on;

/**
 * Denne klassen fungerer som en oversikt over en søknad. Den Lister ut en søknad med tilhørende felter og avhengigheter.
 */
@XmlRootElement(name = "soknad")
public class SoknadStruktur implements Serializable {

    private List<SoknadFaktum> fakta = new ArrayList<>();
    private List<SoknadVedlegg> vedlegg = new ArrayList<>();


    @XmlElement(name = "faktum")
    public List<SoknadFaktum> getFakta() {
        return fakta;
    }

    public void setFakta(List<SoknadFaktum> fakta) {
        this.fakta = fakta;
    }

    @XmlElement(name = "vedlegg")
    public List<SoknadVedlegg> getVedlegg() {
        return vedlegg;
    }

    public void setVedlegg(List<SoknadVedlegg> vedlegg) {
        this.vedlegg = vedlegg;
    }

    @Override
    public String toString() {
        return new StringBuilder("SoknadStruktur{")
                .append(", fakta=").append(fakta)
                .append(", vedlegg=").append(vedlegg)
                .append('}')
                .toString();
    }

    public List<SoknadVedlegg> vedleggFor(final String felt) {
        return on(vedlegg).filter(new Predicate<SoknadVedlegg>() {
            @Override
            public boolean evaluate(SoknadVedlegg soknadVedlegg) {
                return soknadVedlegg.getFaktum().getId().equals(felt);
            }
        }).collect();

    }
}
