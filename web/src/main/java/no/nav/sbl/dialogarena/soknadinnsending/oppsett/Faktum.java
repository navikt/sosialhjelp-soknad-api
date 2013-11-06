package no.nav.sbl.dialogarena.soknadinnsending.oppsett;


import javax.xml.bind.annotation.XmlID;
import java.io.Serializable;

public class Faktum implements Serializable {
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
        final StringBuilder sb = new StringBuilder("Faktum{");
        sb.append("id='").append(id).append('\'');
        sb.append(", type='").append(type).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
