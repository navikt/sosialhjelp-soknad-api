package no.nav.sbl.dialogarena.soknadinnsending.oppsett;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Denne klassen fungerer som en oversikt over en søknad. Den Lister ut en søknad med tilhørende felter og avhengigheter.
 */


@XmlRootElement(name = "soknad")
public class SoknadStruktur implements Serializable {
    private String gosysId;

    private List<Faktum> fakta = new ArrayList<>();
    private List<Vedlegg> vedlegg = new ArrayList<>();

    @XmlAttribute
    public String getGosysId() {
        return gosysId;
    }

    public void setGosysId(String gosysId) {
        this.gosysId = gosysId;
    }

    @XmlElement(name = "faktum")
    public List<Faktum> getFakta() {
        return fakta;
    }

    public void setFakta(List<Faktum> fakta) {
        this.fakta = fakta;
    }

    public List<Vedlegg> getVedlegg() {
        return vedlegg;
    }

    public void setVedlegg(List<Vedlegg> vedlegg) {
        this.vedlegg = vedlegg;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SoknadStruktur{");
        sb.append("gosysId='").append(gosysId).append('\'');
        sb.append(", fakta=").append(fakta);
        sb.append(", vedlegg=").append(vedlegg);
        sb.append('}');
        return sb.toString();
    }
}
