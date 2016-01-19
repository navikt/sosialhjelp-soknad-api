package no.nav.sbl.dialogarena.sendsoknad.domain.dto;

public class Land {
    private String text;
    private String value;

    public Land() {}
    public Land(String text, String value) {
        this.text = text;
        this.value = value;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
