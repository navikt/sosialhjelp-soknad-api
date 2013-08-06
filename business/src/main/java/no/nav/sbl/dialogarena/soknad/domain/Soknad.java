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

    public Long soknadId;
    public String gosysId;
    public Map<String, Faktum> fakta;

    public Soknad() {
        fakta = new LinkedHashMap<>();
    }

    public void leggTilFakta(Map<String, Faktum> fakta) {
        this.fakta.putAll(fakta);
    }
}
