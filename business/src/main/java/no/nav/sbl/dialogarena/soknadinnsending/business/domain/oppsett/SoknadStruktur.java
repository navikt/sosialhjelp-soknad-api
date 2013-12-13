package no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Denne klassen fungerer som en oversikt over en søknad. Den Lister ut en søknad med tilhørende felter og avhengigheter.
 */
@XmlRootElement(name = "soknad")
public class SoknadStruktur implements Serializable {

    private String gosysId;

    private List<SoknadFaktum> fakta = new ArrayList<>();
    private List<SoknadVedlegg> vedlegg = new ArrayList<>();
    private transient Map<String, SoknadVedlegg> vedleggMap;

    @XmlAttribute
    public String getGosysId() {
        return gosysId;
    }

    public void setGosysId(String gosysId) {
        this.gosysId = gosysId;
    }

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
                .append("gosysId='").append(gosysId).append('\'')
                .append(", fakta=").append(fakta)
                .append(", vedlegg=").append(vedlegg)
                .append('}')
                .toString();
    }

    public SoknadVedlegg vedleggFor(String felt) {

        if (vedleggMap == null) {
            vedleggMap = new HashMap<>();
            for (SoknadVedlegg soknadVedlegg : vedlegg) {
                vedleggMap.put(soknadVedlegg.getFaktum().getId(), soknadVedlegg);
            }
        }
        return vedleggMap.get(felt);
    }
}
