package no.nav.sbl.dialogarena.soknad.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Soknad implements Serializable {

    private Long soknadId;
    private String gosysId;
    private Map<String, Faktum> fakta;

    public Soknad() {
        fakta = new LinkedHashMap<>();
    }

    public final Long getSoknadId() {
        return soknadId;
    }

    public final void setSoknadId(Long soknadId) {
        this.soknadId = soknadId;
    }

    public final String getGosysId() {
        return gosysId;
    }

    public final void setGosysId(String gosysId) {
        this.gosysId = gosysId;
    }

    public final Map<String, Faktum> getFakta() {
        return fakta;
    }

    public final void leggTilFakta(Map<String, Faktum> fakta) {
        this.fakta.putAll(fakta);
    }
}
