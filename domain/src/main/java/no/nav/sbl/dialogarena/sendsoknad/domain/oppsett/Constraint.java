package no.nav.sbl.dialogarena.sendsoknad.domain.oppsett;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = {})
public class Constraint {
    private FaktumStruktur faktumStruktur;
    private String expression;

    public Constraint() {
    }

    public Constraint(FaktumStruktur faktum, String expression) {
        this.faktumStruktur = faktum;
        this.expression = expression;
    }

    @XmlIDREF
    @XmlElement(name = "faktum")
    public FaktumStruktur getFaktumStruktur() {
        return faktumStruktur;
    }

    public void setFaktumStruktur(FaktumStruktur faktum) {
        this.faktumStruktur = faktum;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }
}
