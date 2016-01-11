package no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett;

import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = {})
public class Constraint {
    String faktum;
    String expression;

    public Constraint() {
    }

    public Constraint(String faktum, String exception) {
        this.faktum = faktum;
        this.expression = exception;
    }

    public String getFaktum() {
        return faktum;
    }

    public void setFaktum(String faktum) {
        this.faktum = faktum;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }
}
