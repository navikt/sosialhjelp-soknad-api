package no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett;


import javax.xml.bind.annotation.XmlID;
import java.io.Serializable;

public class SoknadFaktum implements Serializable {

    private String id;
    private String type;

    @XmlID()
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return new StringBuilder("SoknadFaktum{")
            .append("id='").append(id).append('\'')
            .append(", type='").append(type).append('\'')
            .append('}')
                .toString();
    }

}
