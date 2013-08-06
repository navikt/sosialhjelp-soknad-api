package no.nav.sbl.dialogarena.soknad.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Soknad implements Serializable {

    public Long soknadId;
    public String gosysId;
    public List<Faktum> fakta;

    public Soknad() {
        fakta = new ArrayList<>();
    }

    public void leggTilFakta(List<Faktum> fakta) {
        this.fakta.addAll(fakta);
    }
}
